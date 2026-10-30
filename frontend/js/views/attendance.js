/* Attendance module — subject-first session flow + history + percentages */
window.view_attendance = async function (view) {
  let students = [], subjects = [], enrollments = [], allRows = [];
  const state = { page: 0, size: 12 };
  /* spis_subject: set when arriving via a faculty dashboard subject card */
  const preselectedSubject = sessionStorage.getItem('spis_subject') || '';

  view.innerHTML =
    '<div class="page-head"><div><h2>Attendance</h2><div class="desc">Pick a subject, mark the enrolled class, save the session. Duplicates for the same student/subject/date are prevented.</div></div>' +
    '<div class="page-actions"><button class="btn ghost" id="exportBtn">⬇ Export</button>' +
    '<button class="btn primary" id="takeBtn">✋ Take Attendance</button></div></div>' +

    '<div class="grid-2-1">' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">Attendance History</h3>' +
    '    <div class="toolbar">' +
    '      <select id="fSubject"><option value="">All subjects</option></select>' +
    '      <select id="fStudent"><option value="">All students</option></select>' +
    '      <select id="fStatus"><option value="">All statuses</option><option>PRESENT</option><option>ABSENT</option></select>' +
    '      <input type="date" id="fFrom"><input type="date" id="fTo">' +
    '      <button class="btn ghost sm" id="clearBtn">Clear</button>' +
    '      <span class="count" id="countLbl"></span>' +
    '    </div>' +
    '    <div class="table-wrap"><table class="data"><thead><tr>' +
    '    <th class="sortable" data-k="attendanceDate">Date ⇅</th><th>Student</th><th>Roll No</th><th>Subject</th><th>Status</th><th>Actions</th>' +
    '    </tr></thead><tbody id="rows"></tbody></table></div>' +
    '    <div class="pager"><button id="prevBtn">← Prev</button><span id="pageInfo"></span><button id="nextBtn">Next →</button></div>' +
    '  </div></div>' +
    '  <div>' +
    '    <div class="card mb"><div class="card-body">' +
    '      <h3 class="card-title">Subject Attendance %</h3><div id="subjChart"></div>' +
    '    </div></div>' +
    '    <div class="card"><div class="card-body">' +
    '      <h3 class="card-title">Low Attendance (below 75%)</h3><div id="lowAtt" class="muted">Loading…</div>' +
    '    </div></div>' +
    '  </div>' +
    '</div>';

  view.insertAdjacentHTML('beforeend', `
    <div class="modal-backdrop" id="takeModal"><div class="modal wide">
      <div class="modal-head"><h3>Take Attendance</h3><button class="x" data-close>✕</button></div>
      <div class="modal-body">
        <div class="form-grid">
          <div class="field"><label>Subject <span class="req">*</span></label><select id="tSubject"><option value="">Select…</option></select></div>
          <div class="field"><label>Date <span class="req">*</span></label><input type="date" id="tDate" required></div>
        </div>
        <div id="takeStatus" class="help mt"></div>
        <div class="flex mt mb">
          <button class="btn ghost sm" id="allPresent">Mark all Present</button>
          <button class="btn ghost sm" id="allAbsent">Mark all Absent</button>
          <span class="muted right" id="tCount"></span>
        </div>
        <div id="takeGrid"></div>
        <div class="form-error" id="takeErr"></div>
        <div class="form-foot"><button type="button" class="btn ghost" data-close>Cancel</button>
        <button class="btn primary" id="takeSave" disabled>Save Attendance</button></div>
      </div></div></div>

    <div class="modal-backdrop" id="editModal"><div class="modal narrow">
      <div class="modal-head"><h3>Edit Attendance</h3><button class="x" data-close>✕</button></div>
      <div class="modal-body"><form id="editForm">
        <div class="field"><label>Date <span class="req">*</span></label><input type="date" name="attendanceDate" required></div>
        <div class="field"><label>Status <span class="req">*</span></label><select name="status" required><option>PRESENT</option><option>ABSENT</option></select></div>
        <div class="form-error"></div>
        <div class="form-foot"><button type="button" class="btn ghost" data-close>Cancel</button>
        <button type="submit" class="btn primary">Save</button></div>
      </form></div></div></div>`);

  initModals();

  async function loadLookups() {
    [students, subjects] = await Promise.all([api('/api/students'), api('/api/subjects')]);
    try { enrollments = await api('/api/enrollments'); } catch (e) { enrollments = []; }
    fillSelect($('#fSubject'), subjects, 'subjectId', s => s.subjectName + ' (' + s.subjectCode + ')', 'All subjects');
    fillSelect($('#fStudent'), students, 'studentId', s => fullName(s) + ' (' + s.rollNumber + ')', 'All students');
    fillSelect($('#tSubject'), subjects, 'subjectId', s => s.subjectName + ' (' + s.subjectCode + ')', 'Select…');
    $('#tDate').value = new Date().toISOString().slice(0, 10);
    if (preselectedSubject) $('#tSubject').value = preselectedSubject;
  }

  async function load() {
    const tbody = $('#rows');
    renderLoading(tbody, 6);
    try {
      const params = {
        subjectId: $('#fSubject').value || undefined,
        studentId: $('#fStudent').value || undefined,
        status: $('#fStatus').value || undefined,
        startDate: $('#fFrom').value || undefined,
        endDate: $('#fTo').value || undefined
      };
      allRows = await api('/api/attendance/search' + qs(params));
      allRows.sort((a, b) => b.attendanceId - a.attendanceId);
      $('#countLbl').textContent = allRows.length + ' record(s)';

      const pg = paginate(allRows, state.page, state.size);
      if (!pg.content.length) renderEmpty(tbody, 6, 'No attendance records found', 'Adjust filters or take attendance.');
      else tbody.innerHTML = pg.content.map(a =>
        '<tr data-id="' + a.attendanceId + '">' +
        '<td>' + fmtDate(a.attendanceDate) + '</td>' +
        '<td><b>' + esc(fullName(a.student)) + '</b></td>' +
        '<td>' + esc(a.student ? a.student.rollNumber : '—') + '</td>' +
        '<td>' + esc(a.subject ? a.subject.subjectName : '—') + '</td>' +
        '<td>' + statusBadge(a.status) + '</td>' +
        '<td class="actions">' +
        '<button class="btn ghost sm" data-act="edit">Edit</button>' +
        '<button class="btn danger sm" data-act="del">Delete</button>' +
        '</td></tr>').join('');
      bindPager(state, pg, load);
    } catch (err) { renderError(tbody, 6, err, load); }
  }

  ['#fStudent', '#fSubject', '#fStatus', '#fFrom', '#fTo'].forEach(sel =>
    $(sel).addEventListener('change', () => { state.page = 0; load(); }));
  $('#clearBtn').onclick = () => {
    ['#fStudent', '#fSubject', '#fStatus', '#fFrom', '#fTo'].forEach(sel => $(sel).value = '');
    state.page = 0; load();
  };

  $all('th.sortable').forEach(th => th.addEventListener('click', () => {
    const k = th.dataset.k;
    const dir = (window.__attSortKey === k) ? -1 : 1;
    window.__attSortKey = k;
    allRows.sort((a, b) => String(a[k]).localeCompare(String(b[k])) * dir);
    state.page = 0; load();
  }));

  /* ---------- take attendance (subject-first, enrolled class only) ---------- */
  function enrolledFor(subjectId) {
    const en = enrollments.filter(e => String(e.subject ? e.subject.subjectId : '') === String(subjectId));
    const list = en.map(e => e.student).filter(Boolean);
    return list.length ? list : students; // fallback: whole roster if no enrollment data
  }

  async function buildTakeGrid() {
    const subjectId = $('#tSubject').value;
    const grid = $('#takeGrid');
    $('#takeStatus').textContent = '';
    if (!subjectId) {
      grid.innerHTML = '<div class="state"><div class="big">👆</div><div class="msg">Pick a subject</div><div class="sub">Enrolled students appear here.</div></div>';
      $('#takeSave').disabled = true;
      return;
    }

    const roster = enrolledFor(subjectId);
    const date = $('#tDate').value;

    /* prefill from an existing session: enables "correction" instead of duplicates */
    let existing = [];
    try {
      existing = await api('/api/attendance/search' + qs({ subjectId, date }));
    } catch (e) { existing = []; }

    grid.innerHTML =
      '<div class="table-wrap" style="max-height:320px;overflow-y:auto"><table class="data"><thead><tr>' +
      '<th>Student</th><th>Roll No</th><th style="width:200px">Status</th></tr></thead><tbody>' +
      roster.map(st => {
        const rec = existing.find(a => a.student && String(a.student.studentId) === String(st.studentId));
        const present = rec ? rec.status === 'PRESENT' : true;
        return '<tr data-id="' + st.studentId + '"' + (rec ? ' data-existing="' + rec.attendanceId + '"' : '') + '><td>' + esc(fullName(st)) + '</td><td>' + esc(st.rollNumber) + '</td>' +
          '<td><div class="flex" style="gap:6px">' +
          '<label class="checkbox-row" style="padding:0"><input type="radio" name="st_' + st.studentId + '" value="PRESENT"' + (present ? ' checked' : '') + '> Present</label>' +
          '<label class="checkbox-row" style="padding:0"><input type="radio" name="st_' + st.studentId + '" value="ABSENT"' + (!present ? ' checked' : '') + '> Absent</label>' +
          '</div></td></tr>';
      }).join('') +
      '</tbody></table></div>';

    $('#takeStatus').textContent = existing.length
      ? 'A session for ' + fmtDate(date) + ' already exists (' + existing.length + ' record(s)). Saving only adds missing students — correct individual rows in history.'
      : roster.length + ' enrolled student(s) will be recorded.';
    $('#takeSave').disabled = false;
    updateCount();
  }

  function updateCount() {
    const roster = enrolledFor($('#tSubject').value);
    const present = roster.filter(st => {
      const r = document.querySelector('input[name="st_' + st.studentId + '"]:checked');
      return r && r.value === 'PRESENT';
    }).length;
    $('#tCount').textContent = present + ' / ' + roster.length + ' present';
  }

  $('#tSubject').addEventListener('change', buildTakeGrid);
  $('#tDate').addEventListener('change', buildTakeGrid);
  $('#takeGrid').addEventListener('change', e => { if (e.target.name && e.target.name.startsWith('st_')) updateCount(); });
  $('#allPresent').onclick = () => { $all('#takeGrid input[value=PRESENT]').forEach(r => r.checked = true); updateCount(); };
  $('#allAbsent').onclick = () => { $all('#takeGrid input[value=ABSENT]').forEach(r => r.checked = true); updateCount(); };

  $('#takeBtn').onclick = () => {
    if (preselectedSubject) { $('#tSubject').value = preselectedSubject; sessionStorage.removeItem('spis_subject'); }
    else $('#tSubject').value = '';
    $('#tDate').value = new Date().toISOString().slice(0, 10);
    buildTakeGrid();
    openModal('takeModal');
  };

  $('#takeSave').onclick = async () => {
    const subjectId = Number($('#tSubject').value);
    const date = $('#tDate').value;
    if (!subjectId || !date) { $('#takeErr').textContent = 'Pick a subject and a date.'; return; }
    $('#takeErr').textContent = '';

    const roster = enrolledFor(subjectId);
    const records = roster.map(st => {
      const radio = document.querySelector('input[name="st_' + st.studentId + '"]:checked');
      return radio ? {
        attendanceDate: date, status: radio.value,
        student: { studentId: st.studentId }, subject: { subjectId }
      } : null;
    }).filter(Boolean);

    await withBusy($('#takeSave'), 'Saving…', async () => {
      try {
        const res = await api('/api/attendance/bulk', { method: 'POST', body: JSON.stringify(records) });
        const skippedNote = res.skipped ? ' · ' + res.skipped + ' already recorded (skipped — edit history to correct)' : '';
        toast(res.saved + ' attendance record(s) saved' + skippedNote + '.', res.saved ? 'success' : 'warn', 'Take attendance');
        closeModal('takeModal');
        load();
        loadSidePanels();
      } catch (e) {
        $('#takeErr').textContent = e.message;
      }
    });
  };

  /* ---------- edit / delete history rows ---------- */
  $('#rows').addEventListener('click', async e => {
    const btn = e.target.closest('button[data-act]');
    if (!btn) return;
    const id = btn.closest('tr').dataset.id;
    if (btn.dataset.act === 'edit') {
      const a = await api('/api/attendance/' + id);
      const form = $('#editForm');
      form.attendanceDate.value = a.attendanceDate;
      form.status.value = a.status;
      form.dataset.id = id;
      clearFormErrors(form);
      openModal('editModal');
    } else {
      const ok = await confirmDialog('Delete Attendance Record?', 'Delete this attendance record permanently?');
      if (!ok) return;
      try { await api('/api/attendance/' + id, { method: 'DELETE' }); toast('Attendance deleted.'); load(); loadSidePanels(); }
      catch (err) { toast(err.message, 'error'); }
    }
  });

  $('#editForm').addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.target;
    const id = form.dataset.id;
    try {
      const existing = await api('/api/attendance/' + id);
      const payload = {
        attendanceDate: form.attendanceDate.value, status: form.status.value,
        student: { studentId: existing.student.studentId },
        subject: { subjectId: existing.subject.subjectId }
      };
      await api('/api/attendance/' + id, { method: 'PUT', body: JSON.stringify(payload) });
      toast('Attendance updated.');
      closeModal('editModal');
      load(); loadSidePanels();
    } catch (err) { showFormErrors(form, err.message); }
  });

  /* ---------- side panels ---------- */
  async function loadSidePanels() {
    try {
      const percents = await Promise.all(subjects.map(s =>
        api('/api/analytics/subject/' + s.subjectId + '/subject-attendance-percentage')
          .catch(() => null)));
      const items = subjects.map((s, i) => ({ label: s.subjectCode, value: percents[i] || 0 }))
        .filter(i => i.value > 0).sort((a, b) => b.value - a.value).slice(0, 8);
      if (items.length) renderBarChart($('#subjChart'), items, { suffix: '%', max: 100, decimals: 0 });
      else $('#subjChart').innerHTML = '<div class="state"><div class="big">📊</div><div class="msg">No attendance data yet</div></div>';
    } catch (e) { $('#subjChart').innerHTML = '<div class="muted">Chart unavailable.</div>'; }

    try {
      const low = await api('/api/analytics/student/below-attendance/75');
      $('#lowAtt').innerHTML = low.length
        ? low.slice(0, 8).map(st => '<div class="list-tile"><div class="ico" style="background:#fef2f2">🚨</div>' +
            '<div class="grow"><b>' + esc(fullName(st)) + '</b><span>' + esc(st.rollNumber) + '</span></div></div>').join('') +
          (low.length > 8 ? '<div class="muted mt">+ ' + (low.length - 8) + ' more…</div>' : '')
        : '<div class="state"><div class="big">🎉</div><div class="msg">Everyone is above 75%</div></div>';
    } catch (e) { $('#lowAtt').innerHTML = '<div class="muted">Unavailable.</div>'; }
  }

  $('#exportBtn').onclick = async () => {
    if (!allRows.length) { toast('Nothing to export yet.', 'warn'); return; }
    const rows = allRows.map(a => ({
      date: a.attendanceDate, student: fullName(a.student), roll: a.student ? a.student.rollNumber : '',
      subject: a.subject ? a.subject.subjectName : '', status: a.status
    }));
    await exportWithPicker('attendance', [
      { label: 'Date', value: 'date' }, { label: 'Student', value: 'student' }, { label: 'Roll No', value: 'roll' },
      { label: 'Subject', value: 'subject' }, { label: 'Status', value: 'status' }
    ], rows);
  };

  await loadLookups();
  await Promise.all([load(), loadSidePanels()]);
};
