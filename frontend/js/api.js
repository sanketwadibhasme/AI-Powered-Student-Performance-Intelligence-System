/* ============================================================
   SPIS API + auth + shared helpers (loaded by every page)
   ============================================================ */
const API = 'http://localhost:8080';

/* Request timeouts (AbortController): normal APIs 30s, AI inference 60s.
   The AI timeout is longer because model inference can legitimately take a
   while; a hung backend must never leave the UI spinning forever. */
const API_TIMEOUT_MS = 30000;
const AI_TIMEOUT_MS = 60000;

/* ---------- session ---------- */
function getToken() { return sessionStorage.getItem('spis_token'); }
function getRole() { return sessionStorage.getItem('spis_role'); }
function getUsername() { return sessionStorage.getItem('spis_username'); }

function requireAuth(allowedRoles) {
  if (!getToken()) { window.location.href = 'login.html'; return false; }
  if (allowedRoles && !allowedRoles.includes(getRole())) { window.location.href = 'login.html'; return false; }
  return true;
}

function logout() {
  sessionStorage.clear();
  window.location.href = 'login.html';
}

/* ---------- authenticated fetch ----------
   Every request carries an AbortController timeout. On timeout a clear
   Error is thrown (err.timeout = true) so callers can show a useful
   message instead of waiting forever. */
async function api(path, options = {}) {
  const headers = Object.assign(
    { 'Content-Type': 'application/json' },
    options.headers || {}
  );
  headers['Authorization'] = 'Bearer ' + getToken();

  const isAI = path.startsWith('/api/ai/');
  const timeoutMs = isAI ? AI_TIMEOUT_MS : API_TIMEOUT_MS;
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);

  let res;
  try {
    res = await fetch(API + path, Object.assign({}, options, { headers, signal: controller.signal }));
  } catch (e) {
    clearTimeout(timer);
    if (e && e.name === 'AbortError') {
      const err = new Error(isAI
        ? 'The AI prediction request timed out. Please verify that the AI service is running and try again.'
        : 'The request timed out. Check your connection and try again.');
      err.timeout = true;
      throw err;
    }
    const err = new Error('Cannot reach the server. Check that the backend is running on ' + API + '.');
    err.network = true;
    throw err;
  }
  clearTimeout(timer);

  if (res.status === 401) {
    sessionStorage.clear();
    window.location.href = 'login.html';
    const err = new Error('Session expired. Please sign in again.');
    err.status = 401;
    throw err;
  }

  // 204 No Content
  if (res.status === 204) return null;

  const data = await res.json().catch(() => null);

  if (!res.ok) {
    const message = data && data.message ? data.message
      : (data && data.error ? data.error
      : 'Request failed (HTTP ' + res.status + ')');
    const err = new Error(message);
    err.status = res.status;
    err.data = data;
    throw err;
  }
  return data;
}

/* ---------- DOM + formatting helpers ---------- */
function $(sel, root) { return (root || document).querySelector(sel); }
function $all(sel, root) { return Array.from((root || document).querySelectorAll(sel)); }

function esc(v) {
  if (v === null || v === undefined) return '';
  return String(v)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

function fullName(p) {
  if (!p) return '—';
  return [p.firstName, p.lastName].filter(Boolean).join(' ') || '—';
}

function num(v, digits) {
  if (v === null || v === undefined || isNaN(v)) return '—';
  return Number(v).toFixed(digits === undefined ? 1 : digits);
}

function pct(v) {
  if (v === null || v === undefined || isNaN(v)) return '—';
  return Number(v).toFixed(1) + '%';
}

function fmtDate(v) {
  if (!v) return '—';
  return String(v).split('T')[0];
}

function initials(name) {
  return String(name || '?').split(' ').filter(Boolean).map(w => w[0].toUpperCase()).slice(0, 2).join('');
}

/* Build a query string from an object, skipping empty values. */
function qs(params) {
  const parts = [];
  Object.keys(params || {}).forEach(k => {
    const v = params[k];
    if (v !== undefined && v !== null && v !== '') parts.push(encodeURIComponent(k) + '=' + encodeURIComponent(v));
  });
  return parts.length ? '?' + parts.join('&') : '';
}

/* ---------- badges ---------- */
function statusBadge(status) {
  const s = String(status || '').toUpperCase();
  const map = {
    ACTIVE: 'green', GRADUATED: 'blue', SUSPENDED: 'red', DROPPED: 'gray',
    PRESENT: 'green', ABSENT: 'red',
    MALE: 'blue', FEMALE: 'violet', OTHER: 'gray',
    ADMIN: 'violet', FACULTY: 'blue', STUDENT: 'green',
    LOW: 'green', MEDIUM: 'amber', HIGH: 'red',
    EXCELLENT: 'green', GOOD: 'green', AVERAGE: 'amber', POOR: 'red'
  };
  return '<span class="badge ' + (map[s] || 'gray') + '">' + esc(status || '—') + '</span>';
}

function marksBadge(avg) {
  if (avg === null || avg === undefined || isNaN(avg)) return '<span class="badge gray">No data</span>';
  const v = Number(avg);
  const cls = v >= 70 ? 'green' : v >= 50 ? 'amber' : 'red';
  return '<span class="badge ' + cls + '">' + v.toFixed(1) + '</span>';
}

function attBadge(p) {
  if (p === null || p === undefined || isNaN(p)) return '<span class="badge gray">No data</span>';
  const v = Number(p);
  const cls = v >= 75 ? 'green' : v >= 60 ? 'amber' : 'red';
  return '<span class="badge ' + cls + '">' + v.toFixed(1) + '%</span>';
}

/* ---------- toast notifications ---------- */
function toast(msg, type, title) {
  let root = $('#toastRoot');
  if (!root) {
    root = document.createElement('div');
    root.id = 'toastRoot';
    document.body.appendChild(root);
  }
  const el = document.createElement('div');
  el.className = 'toast ' + (type || 'success');
  el.innerHTML = '<b>' + esc(title || (type === 'error' ? 'Error' : type === 'warn' ? 'Warning' : 'Success')) + '</b>' + esc(msg);
  root.appendChild(el);
  setTimeout(() => { el.style.opacity = '0'; el.style.transition = 'opacity .3s'; setTimeout(() => el.remove(), 320); }, 3800);
}

/* ---------- modal helpers ---------- */
function openModal(id) { $('#' + id).classList.add('open'); }
function closeModal(id) { $('#' + id).classList.remove('open'); }

/* Wire up all modals in the page: backdrop click + [data-close] buttons + Esc.
   Idempotent: safe to call on every boot and from every view — each backdrop,
   close button and the document Esc listener is wired only once. */
function initModals() {
  $all('.modal-backdrop').forEach(bd => {
    if (bd.dataset.modalWired) return;
    bd.dataset.modalWired = '1';
    bd.addEventListener('mousedown', e => { if (e.target === bd) bd.classList.remove('open'); });
  });
  $all('[data-close]').forEach(btn => {
    if (btn.dataset.closeWired) return;
    btn.dataset.closeWired = '1';
    btn.addEventListener('click', () => {
      const bd = btn.closest('.modal-backdrop');
      if (bd) bd.classList.remove('open');
    });
  });
  if (!window.__spisEscWired) {
    window.__spisEscWired = true;
    document.addEventListener('keydown', e => {
      if (e.key === 'Escape') $all('.modal-backdrop.open').forEach(bd => bd.classList.remove('open'));
    });
  }
}

/* ---------- form helpers ---------- */
function formToJSON(form) {
  const data = {};
  new FormData(form).forEach((value, key) => {
    if (value === '') return; // skip empty -> server treats as absent
    data[key] = value;
  });
  return data;
}

/* Convert numeric fields (marked with data-type="number") to numbers. */
function coerceJSON(form, json) {
  $all('[name]', form).forEach(el => {
    if (el.dataset.type === 'number' && json[el.name] !== undefined) {
      json[el.name] = Number(json[el.name]);
    }
  });
  return json;
}

function showFormErrors(form, message) {
  // simple: surface the backend message in the first .form-error slot on the form
  let slot = $('.form-error', form);
  if (!slot) {
    slot = document.createElement('div');
    slot.className = 'form-error';
    slot.style.cssText = 'color:#dc2626;font-size:12.5px;margin-top:10px;';
    form.prepend(slot);
  }
  slot.textContent = message;
}

function clearFormErrors(form) {
  const slot = $('.form-error', form);
  if (slot) slot.textContent = '';
}

/* ---------- table states ---------- */
function renderLoading(tbody, cols) {
  tbody.innerHTML = '<tr><td colspan="' + cols + '"><div class="state"><div class="spin"></div>Loading…</div></td></tr>';
}

function renderEmpty(tbody, cols, msg, sub) {
  tbody.innerHTML = '<tr><td colspan="' + cols + '"><div class="state"><div class="big">📭</div>' +
    '<div class="msg">' + esc(msg || 'No records found') + '</div>' +
    '<div class="sub">' + esc(sub || 'Try changing your filters.') + '</div></div></td></tr>';
}

function renderError(tbody, cols, err, retryFn) {
  tbody.innerHTML = '<tr><td colspan="' + cols + '"><div class="state error"><div class="big">⚠️</div>' +
    '<div class="msg">' + esc(err.message || 'Unable to load data') + '</div>' +
    (retryFn ? '<button class="btn ghost sm" id="retryBtn">Retry</button>' : '') +
    '</div></td></tr>';
  if (retryFn) { const b = $('#retryBtn', tbody); if (b) b.onclick = retryFn; }
}

/* ---------- client-side pagination for list APIs ---------- */
function paginate(list, page, size) {
  const total = list.length;
  const totalPages = Math.max(1, Math.ceil(total / size));
  const safePage = Math.min(Math.max(0, page), totalPages - 1);
  return {
    content: list.slice(safePage * size, safePage * size + size),
    number: safePage, totalPages, totalElements: total,
    first: safePage === 0, last: safePage >= totalPages - 1
  };
}

/* Wire pager buttons. state = {page}, pg = Spring Page object, reload = fn. */
function bindPager(state, pg, reload) {
  const prev = $('#prevBtn'), next = $('#nextBtn'), info = $('#pageInfo');
  if (!prev || !next || !info) return;
  info.textContent = 'Page ' + (pg.number + 1) + ' of ' + pg.totalPages +
    ' (' + pg.totalElements + ' record' + (pg.totalElements === 1 ? '' : 's') + ')';
  prev.disabled = pg.first;
  next.disabled = pg.last;
  prev.onclick = () => { if (state.page > 0) { state.page--; reload(); } };
  next.onclick = () => { if (!pg.last) { state.page++; reload(); } };
}

/* ---------- generic column sorter for rendered tables ---------- */
let sortState = { key: null, dir: 1 };
function sortRows(rows, keyFn, key) {
  if (sortState.key !== key) { sortState = { key, dir: 1 }; }
  else { sortState.dir = -sortState.dir; }
  const dir = sortState.dir;
  return rows.slice().sort((a, b) => {
    const va = keyFn(a), vb = keyFn(b);
    if (va === null || va === undefined) return 1;
    if (vb === null || vb === undefined) return -1;
    if (typeof va === 'number' && typeof vb === 'number') return (va - vb) * dir;
    return String(va).localeCompare(String(vb)) * dir;
  });
}

/* ---------- CSV / Excel export (real file download, no fake links) ---------- */
function exportCSV(filename, columns, rows) {
  const head = columns.map(c => '"' + c.label.replace(/"/g, '""') + '"').join(',');
  const body = rows.map(r =>
    columns.map(c => {
      const v = typeof c.value === 'function' ? c.value(r) : r[c.value];
      return '"' + String(v === null || v === undefined ? '' : v).replace(/"/g, '""') + '"';
    }).join(',')
  ).join('\r\n');
  downloadBlob('\uFEFF' + head + '\r\n' + body, filename + '.csv', 'text/csv;charset=utf-8');
  toast(columns.length + ' columns × ' + rows.length + ' rows exported.', 'success', 'Export complete');
}

function exportPrint(filename, title, columns, rows) {
  // Opens a print-ready document (browser "Save as PDF" produces the PDF).
  const w = window.open('', '_blank');
  if (!w) { toast('Allow pop-ups to export PDF.', 'warn'); return; }
  const head = columns.map(c => '<th>' + esc(c.label) + '</th>').join('');
  const body = rows.map(r =>
    '<tr>' + columns.map(c => {
      const v = typeof c.value === 'function' ? c.value(r) : r[c.value];
      return '<td>' + esc(v === null || v === undefined ? '' : v) + '</td>';
    }).join('') + '</tr>').join('');
  w.document.write(
    '<html><head><title>' + esc(title) + '</title><style>' +
    'body{font-family:Segoe UI,Arial,sans-serif;color:#0f172a;padding:24px;}' +
    'h2{margin:0 0 2px;} .sub{color:#64748b;font-size:12px;margin-bottom:16px;}' +
    'table{border-collapse:collapse;width:100%;font-size:12px;}' +
    'th{background:#0f172a;color:#fff;padding:7px 9px;text-align:left;}' +
    'td{padding:6px 9px;border-bottom:1px solid #e2e8f0;}' +
    'tr:nth-child(even) td{background:#f8fafc;}' +
    '@media print{ .noprint{display:none;} }' +
    '</style></head><body>' +
    '<h2>' + esc(title) + '</h2>' +
    '<div class="sub">Student Performance Intelligence System · generated ' + new Date().toLocaleString() + ' · ' + rows.length + ' rows</div>' +
    '<table><thead><tr>' + head + '</tr></thead><tbody>' + body + '</tbody></table>' +
    '<p class="noprint" style="margin-top:18px"><button onclick="window.print()">Print / Save as PDF</button></p>' +
    '</body></html>');
  w.document.close();
  toast('Use "Save as PDF" in the print dialog.', 'success', 'Export ready');
}

function downloadBlob(content, filename, mime) {
  const blob = new Blob([content], { type: mime });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url; a.download = filename;
  document.body.appendChild(a); a.click(); a.remove();
  URL.revokeObjectURL(url);
}

/* ---------- simple bar chart renderer (pure CSS/HTML) ---------- */
function renderBarChart(el, items, opts) {
  // items: [{label, value, cls?}] ; opts: {suffix, max, decimals}
  if (!items.length) {
    el.innerHTML = '<div class="state"><div class="big">📊</div><div class="msg">No data to chart</div></div>';
    return;
  }
  const max = opts && opts.max ? opts.max : Math.max(...items.map(i => i.value), 1);
  const dec = opts && opts.decimals !== undefined ? opts.decimals : 1;
  const suffix = opts && opts.suffix ? opts.suffix : '';
  el.innerHTML = '<div class="chart-bars">' + items.map(i => {
    const h = Math.max(2, Math.round((i.value / max) * 100));
    return '<div class="bar-col">' +
      '<div class="bar ' + (i.cls || '') + '" style="height:' + h + '%">' +
      '<span class="val">' + num(i.value, dec) + suffix + '</span></div>' +
      '<div class="bar-lbl" title="' + esc(i.label) + '">' + esc(i.label) + '</div>' +
      '</div>';
  }).join('') + '</div>';
}

/* ---------- confirm dialog (returns Promise<boolean>) ---------- */
function confirmDialog(title, message, confirmLabel) {
  return new Promise(resolve => {
    let bd = $('#confirmModal');
    if (!bd) {
      bd = document.createElement('div');
      bd.id = 'confirmModal';
      bd.className = 'modal-backdrop';
      bd.innerHTML =
        '<div class="modal narrow"><div class="modal-head"><h3 id="cfTitle"></h3>' +
        '<button class="x" data-x>✕</button></div>' +
        '<div class="modal-body"><p id="cfMsg"></p></div>' +
        '<div class="modal-foot"><button class="btn ghost" data-no>Cancel</button>' +
        '<button class="btn danger" data-yes>Delete</button></div></div>';
      document.body.appendChild(bd);
    }
    $('#cfTitle', bd).textContent = title;
    $('#cfMsg', bd).textContent = message;
    $('#confirmModal .modal-foot [data-yes]').textContent = confirmLabel || 'Delete';
    bd.classList.add('open');

    const done = val => { bd.classList.remove('open'); cleanup(); resolve(val); };
    const cleanup = () => {
      $('[data-yes]', bd).onclick = null; $('[data-no]', bd).onclick = null;
      $('[data-x]', bd).onclick = null; bd.onmousedown = null;
    };
    $('[data-yes]', bd).onclick = () => done(true);
    $('[data-no]', bd).onclick = () => done(false);
    $('[data-x]', bd).onclick = () => done(false);
    bd.onmousedown = e => { if (e.target === bd) done(false); };
  });
}

/* ---------- select helper ---------- */
function fillSelect(select, list, valueKey, labelFn, placeholder) {
  select.innerHTML = (placeholder ? '<option value="">' + esc(placeholder) + '</option>' : '') +
    list.map(item => '<option value="' + esc(item[valueKey]) + '">' + esc(labelFn(item)) + '</option>').join('');
}

/* Disable a submit button while a promise runs; restore label afterwards.
   Returns fn()'s result so callers can use it (e.g. runAI predictions). */
async function withBusy(btn, labelBusy, fn) {
  if (btn.disabled) return; // concurrent-request guard: ignore double-clicks
  const original = btn.textContent;
  btn.disabled = true;
  btn.textContent = labelBusy;
  try { return await fn(); }
  finally { btn.disabled = false; btn.textContent = original; }
}

/* ---------- column picker for exports ----------
   columns: [{label, value}] ; returns the chosen subset or null if cancelled. */
function pickColumns(columns) {
  const id = 'colPickModal';
  if (!$('#' + id)) {
    document.body.insertAdjacentHTML('beforeend',
      '<div class="modal-backdrop" id="' + id + '"><div class="modal narrow">' +
      '<div class="modal-head"><h3>Choose columns</h3><button class="x" data-close>✕</button></div>' +
      '<div class="modal-body" id="colPickBody"></div>' +
      '<div class="modal-foot"><button class="btn ghost" data-close>Cancel</button>' +
      '<button class="btn primary" id="colPickOk">Apply</button></div></div></div>');
    initModals();
  }
  $('#colPickBody').innerHTML = columns.map((c, i) =>
    '<label class="checkbox-row"><input type="checkbox" data-i="' + i + '" checked> ' + esc(c.label) + '</label>').join('');
  openModal(id);
  return new Promise(resolve => {
    $('#colPickOk').onclick = () => {
      const chosen = $all('#colPickBody input:checked').map(cb => columns[cb.dataset.i]);
      closeModal(id);
      resolve(chosen.length ? chosen : null);
    };
  });
}

/* Convenience: run the picker then download CSV for the chosen columns. */
async function exportWithPicker(baseName, columns, rows) {
  const chosen = await pickColumns(columns);
  if (!chosen) return;
  exportCSV(baseName, chosen, rows);
}

/* Convenience: run the picker then open the print/PDF view. */
async function exportPdfWithPicker(title, columns, rows) {
  const chosen = await pickColumns(columns);
  if (!chosen) return;
  exportPrint(title, title, chosen, rows);
}
