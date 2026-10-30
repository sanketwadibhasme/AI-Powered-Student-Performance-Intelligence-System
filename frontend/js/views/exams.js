/* Exams module — CRUD for exam schedule */
window.view_exams = async function (view) {
  const state = { sortKey: 'examDate', sortDir: 1 };
  let allRows = [];

  view.innerHTML =
    '<div class="page-head"><div><h2>Exams</h2><div class="desc">Create exams and define their dates and total marks.</div></div>' +
    '<div class="page-actions"><button class="btn ghost" id="exportBtn">⬇ Export</button>' +
    '<button class="btn primary" id="addBtn">＋ Add Exam</button></div></div>' +

    '<div class="card"><div class="card-body">' +
    '<div class="toolbar">' +
    '  <div class="search">🔍<input id="q" placeholder="Search exam name…"></div>' +
    '  <span class="count" id="countLbl"></span>' +
    '</div>' +
    '<div class="table-wrap"><table class="data"><thead><tr>' +
    '<th class="sortable" data-k="examDate">Date ⇅</th>' +
    '<th class="sortable" data-k="examName">Exam ⇅</th>' +
    '<th class="sortable" data-k="totalMarks">Total Marks ⇅</th>' +
    '<th>Marks Entries</th><th>Actions</th>' +
    '</tr></thead><tbody id="rows"></tbody></table></div>' +
    '</div></div>';

  view.insertAdjacentHTML('beforeend',
    '<div class="modal-backdrop" id="examModal"><div class="modal">' +
    '<div class="modal-head"><h3 id="xmTitle">Add Exam</h3><button class="x" data-close>✕</button></div>' +
    '<div class="modal-body"><form id="examForm"><div class="form-grid">' +
    '<div class="field full"><label>Exam Name <span class="req">*</span></label><input name="examName" required minlength="2" maxlength="50" placeholder="e.g. Mid-Term Examination"></div>' +
    '<div class="field"><label>Exam Date <span class="req">*</span></label><input name="examDate" type="date" required></div>' +
    '<div class="field"><label>Total Marks <span class="req">*</span></label><input name="totalMarks" data-type="number" type="number" min="1" max="200" required></div>' +
    '</div><div class="form-error"></div>' +
    '<div class="form-foot"><button type="button" class="btn ghost" data-close>Cancel</button>' +
    '<button type="submit" class="btn primary" id="saveBtn">Save Exam</button></div>' +
    '</form></div></div></div>');

  initModals();

  async function load() {
    const tbody = $('#rows');
    renderLoading(tbody, 5);
    try {
      allRows = await api('/api/exams');
      const q = ($('#q').value || '').trim().toLowerCase();
      if (q) allRows = allRows.filter(x => (x.examName || '').toLowerCase().includes(q));
      allRows.sort((a, b) => {
        const va = a[state.sortKey], vb = b[state.sortKey];
        if (va == null) return 1; if (vb == null) return -1;
        return String(va).localeCompare(String(vb)) * state.sortDir;
      });
      $('#countLbl').textContent = allRows.length + ' exam(s)';

      if (!allRows.length) renderEmpty(tbody, 5, 'No exams yet', 'Create an exam before entering marks.');
      else {
        const marks = await api('/api/marks');
        const countBy = {};
        marks.forEach(m => { const id = m.exam ? m.exam.examId : null; if (id) countBy[id] = (countBy[id] || 0) + 1; });
        tbody.innerHTML = allRows.map(x =>
          '<tr data-id="' + x.examId + '">' +
          '<td>' + fmtDate(x.examDate) + '</td>' +
          '<td><b>' + esc(x.examName) + '</b></td>' +
          '<td>' + x.totalMarks + '</td>' +
          '<td>' + (countBy[x.examId] || 0) + '</td>' +
          '<td class="actions">' +
          '<button class="btn ghost sm" data-act="edit">Edit</button>' +
          '<button class="btn danger sm" data-act="del">Delete</button>' +
          '</td></tr>').join('');
      }
    } catch (err) { renderError(tbody, 5, err, load); }
  }

  $('#q').addEventListener('input', load);
  $all('th.sortable').forEach(th => th.addEventListener('click', () => {
    const k = th.dataset.k;
    state.sortDir = (state.sortKey === k) ? -state.sortDir : 1;
    state.sortKey = k; load();
  }));

  function openForm(x) {
    const form = $('#examForm');
    form.reset(); clearFormErrors(form);
    $('#xmTitle').textContent = x ? 'Edit Exam — ' + x.examName : 'Add Exam';
    if (x) {
      form.examName.value = x.examName;
      form.examDate.value = x.examDate;
      form.totalMarks.value = x.totalMarks;
    }
    form.dataset.id = x ? x.examId : '';
    openModal('examModal');
  }

  $('#addBtn').onclick = () => openForm(null);

  $('#examForm').addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.target;
    const json = coerceJSON(form, formToJSON(form));
    const id = form.dataset.id;
    try {
      if (id) { await api('/api/exams/' + id, { method: 'PUT', body: JSON.stringify(json) }); toast('Exam updated successfully.'); }
      else { await api('/api/exams', { method: 'POST', body: JSON.stringify(json) }); toast('Exam created successfully.'); }
      closeModal('examModal');
      load();
    } catch (err) { showFormErrors(form, err.message); }
  });

  $('#rows').addEventListener('click', async e => {
    const btn = e.target.closest('button[data-act]');
    if (!btn) return;
    const id = btn.closest('tr').dataset.id;
    if (btn.dataset.act === 'edit') {
      openForm(await api('/api/exams/' + id));
    } else {
      const ok = await confirmDialog('Delete Exam?', 'Are you sure you want to delete this exam? Marks entries for it may block deletion.');
      if (!ok) return;
      try { await api('/api/exams/' + id, { method: 'DELETE' }); toast('Exam deleted successfully.'); load(); }
      catch (err) { toast(err.message, 'error'); }
    }
  });

  $('#exportBtn').onclick = async () => {
    if (!allRows.length) { toast('Nothing to export yet.', 'warn'); return; }
    const rows = allRows.map(x => ({ name: x.examName, date: x.examDate, total: x.totalMarks }));
    await exportWithPicker('exams', [
      { label: 'Exam', value: 'name' }, { label: 'Date', value: 'date' }, { label: 'Total Marks', value: 'total' }
    ], rows);
  };

  await load();
};
