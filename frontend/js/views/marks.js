/* Marks module — class entry by subject & exam + CRUD + filters */
window.view_marks = async function (view) {
  let students = [], subjects = [], exams = [], enrollments = [], allRows = [];
  const state = { page: 0, size: 10 };
  /* spis_subject: set when arriving via a faculty dashboard subject card */
  const preselectedSubject = sessionStorage.getItem('spis_subject') || '';

  view.innerHTML =
    '<div class="page-head"><div><h2>Marks</h2><div class="desc">Enter marks per subject &amp; exam for the enrolled class, or manage individual records below.</div></div>' +
    '<div class="page-actions"><button class="btn ghost" id="exportBtn">⬇ Export</button>' +
    '<button class="btn ghost" id="bulkBtn">⧉ Class Entry</button>' +
    '<button class="btn primary" id="addBtn">＋ Add Marks</button></div></div>' +

    '<div class="card"><div class="card-body">' +
    '<div class="toolbar">' +
    '  <select id="fSubject"><option value="">All subjects</option></select>' +
    '  <select id="fExam"><option value="">All exams</option></select>' +
    '  <select id="fStudent"><option value="">All students</option></select>' +
    '  <input type="number" id="fMin" placeholder="Min marks" style="width:110px;height:37px;border:1px solid var(--border);border-radius:8px;padding:0 10px;font-size:13px">' +
    '  <input type="number" id="fMax" placeholder="Max marks" style="width:110px;height:37px;border:1px solid var(--border);border-radius:8px;padding:0 10px;font-size:13px">' +
    '  <button class="btn ghost sm" id="clearBtn">Clear</button>' +
    '  <span class="count" id="countLbl"></span>' +
    '</div>' +
    '<div class="table-wrap"><table class="data"><thead><tr>' +
    '<th>Student</th><th>Roll No</th><th>Subject</th><th>Exam</th><th>Marks</th><th>Actions</th>' +
    '</tr></thead><tbody id="rows"></tbody></table></div>' +
    '<div class="pager"><button id="prevBtn">← Prev</button><span id="pageInfo"></span><button id="nextBtn">Next →</button></div>' +
    '</div></div>';

  view.insertAdjacentHTML('beforeend', `
    <div class="modal-backdrop" id="marksModal"><div class="modal wide">
      <div class="modal-head"><h3 id="mmTitle">Add Marks</h3><button class="x" data-close>✕</button></div>
      <div class="modal-body"><form id="marksForm"><div class="form-grid">
        <div class="field"><label>Student <span class="req">*</span></label><select name="studentId" required><option value="">Select…</option></select></div>
        <div class="field"><label>Subject <span class="req">*</span></label><select name="subjectId" required><option value="">Select…</option></select></div>
        <div class="field"><label>Exam <span class="req">*</span></label><select name="examId" required><option value="">Select…</option></select></div>
        <div class="field"><label>Marks Obtained <span class="req">*</span></label><input name="marksObtained" data-type="number" type="number" step="0.5" min="0" required>
          <div class="help" id="maxHelp"></div></div>
      </div><div class="form-error"></div>
      <div class="form-foot"><button type="button" class="btn ghost" data-close>Cancel</button>
      <button type="submit" class="btn primary" id="saveBtn">Save Marks</button></div>
      </form></div></div></div>

    <div class="modal-backdrop" id="bulkModal"><div class="modal wide">
      <div class="modal-head"><h3>Class Marks Entry</h3><button class="x" data-close>✕</button></div>
      <div class="modal-body">
        <div class="form-grid">
          <div class="field"><label>Subject <span class="req">*</span></label><select id="bSubject"><option value="">Select…</option></select></div>
          <div class="field"><label>Exam <span class="req">*</span></label><select id="bExam"><option value="">Select…</option></select></div>
        </div>
        <div id="bulkStatus" class="help mt"></div>
        <div id="bulkGrid" class="mt"></div>
        <div class="form-error" id="bulkErr"></div>
        <div class="form-foot"><button type="button" class="btn ghost" data-close>Cancel</button>
        <button class="btn primary" id="bulkSaveBtn" disabled>Save All</button></div>
      </div></div></div>`);

  initModals();

  async function loadLookups() {
    [students, subjects, exams] = await Promise.all([api('/api/students'), api('/api/subjects'), api('/api/exams')]);
    try { enrollments = await api('/api/enrollments'); } catch (e) { enrollments = []; }
    const stOpt = s => fullName(s) + ' (' + s.rollNumber + ')';
    fillSelect($('#fStudent'), students, 'studentId', stOpt, 'All students');
    fillSelect($('#fSubject'), subjects, 'subjectId', s => s.subjectName + ' (' + s.subjectCode + ')', 'All subjects');
    fillSelect($('#fExam'), exams, 'examId', x => x.examName, 'All exams');
    fillSelect($('#marksForm [name=studentId]'), students, 'studentId', stOpt, 'Select…');
    fillSelect($('#marksForm [name=subjectId]'), subjects, 'subjectId', s => s.subjectName, 'Select…');
    fillSelect($('#marksForm [name=examId]'), exams, 'examId', x => x.examName + ' (max ' + x.totalMarks + ')', 'Select…');
    fillSelect($('#bSubject'), subjects, 'subjectId', s => s.subjectName, 'Select…');
    fillSelect($('#bExam'), exams, 'examId', x => x.examName + ' (max ' + x.totalMarks + ')', 'Select…');
    $('#marksForm [name=examId]').addEventListener('change', e => {
      const x = exams.find(v => String(v.examId) === e.target.value);
      $('#maxHelp').textContent = x ? 'Total marks for this exam: ' + x.totalMarks : '';
    });
    if (preselectedSubject) $('#bSubject').value = preselectedSubject;
  }

  function filtersSet() {
    return $('#fStudent').value || $('#fSubject').value || $('#fExam').value || $('#fMin').value || $('#fMax').value;
  }

  async function load() {
    const tbody = $('#rows');
    renderLoading(tbody, 6);
    try {
      const params = {
        studentId: $('#fStudent').value || undefined,
        subjectId: $('#fSubject').value || undefined,
        examId: $('#fExam').value || undefined,
        minMarks: $('#fMin').value || undefined,
        maxMarks: $('#fMax').value || undefined
      };
      allRows = await api('/api/marks/search' + qs(params));
      // newest entries (highest id) first
      allRows.sort((a, b) => b.marksId - a.marksId);
      $('#countLbl').textContent = allRows.length + ' record(s)';

      const pg = paginate(allRows, state.page, state.size);
      if (!pg.content.length) renderEmpty(tbody, 6, 'No marks records found', 'Adjust filters or add marks.');
      else tbody.innerHTML = pg.content.map(m =>
        '<tr data-id="' + m.marksId + '">' +
        '<td><b>' + esc(fullName(m.student)) + '</b></td>' +
        '<td>' + esc(m.student ? m.student.rollNumber : '—') + '</td>' +
        '<td>' + esc(m.subject ? m.subject.subjectName : '—') + '</td>' +
        '<td>' + esc(m.exam ? m.exam.examName : '—') + '</td>' +
        '<td>' + marksBadge(m.marksObtained) + '</td>' +
        '<td class="actions">' +
        '<button class="btn ghost sm" data-act="edit">Edit</button>' +
        '<button class="btn danger sm" data-act="del">Delete</button>' +
        '</td></tr>').join('');
      bindPager(state, pg, load);
    } catch (err) { renderError(tbody, 6, err, load); }
  }

  ['#fStudent', '#fSubject', '#fExam', '#fMin', '#fMax'].forEach(sel =>
    $(sel).addEventListener('change', () => { state.page = 0; load(); }));
  $('#clearBtn').onclick = () => {
    ['#fStudent', '#fSubject', '#fExam', '#fMin', '#fMax'].forEach(sel => $(sel).value = '');
    state.page = 0; load();
  };

  /* ---------- single CRUD ---------- */
  function openForm(m) {
    const form = $('#marksForm');
    form.reset(); clearFormErrors(form);
    $('#mmTitle').textContent = m ? 'Edit Marks #' + m.marksId : 'Add Marks';
    if (m) {
      form.studentId.value = m.student ? m.student.studentId : '';
      form.subjectId.value = m.subject ? m.subject.subjectId : '';
      form.examId.value = m.exam ? m.exam.examId : '';
      form.marksObtained.value = m.marksObtained;
      const x = exams.find(v => String(v.examId) === String(form.examId.value));
      $('#maxHelp').textContent = x ? 'Total marks for this exam: ' + x.totalMarks : '';
    }
    form.dataset.id = m ? m.marksId : '';
    openModal('marksModal');
  }

  $('#addBtn').onclick = () => openForm(null);

  $('#marksForm').addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.target;
    const json = coerceJSON(form, formToJSON(form));
    const payload = {
      marksObtained: json.marksObtained,
      student: { studentId: Number(json.studentId) },
      subject: { subjectId: Number(json.subjectId) },
      exam: { examId: Number(json.examId) }
    };
    const id = form.dataset.id;
    try {
      if (id) { await api('/api/marks/' + id, { method: 'PUT', body: JSON.stringify(payload) }); toast('Marks updated successfully.'); }
      else { await api('/api/marks', { method: 'POST', body: JSON.stringify(payload) }); toast('Marks saved successfully.'); }
      closeModal('marksModal');
      load();
    } catch (err) { showFormErrors(form, err.message); }
  });

  $('#rows').addEventListener('click', async e => {
    const btn = e.target.closest('button[data-act]');
    if (!btn) return;
    const id = btn.closest('tr').dataset.id;
    if (btn.dataset.act === 'edit') {
      openForm(await api('/api/marks/' + id));
    } else {
      const ok = await confirmDialog('Delete Marks Entry?', 'Delete this marks record permanently?');
      if (!ok) return;
      try { await api('/api/marks/' + id, { method: 'DELETE' }); toast('Marks deleted successfully.'); load(); }
      catch (err) { toast(err.message, 'error'); }
    }
  });

  /* ---------- class entry ---------- */
  function enrolledFor(subjectId) {
    const en = enrollments.filter(e => String(e.subject ? e.subject.subjectId : '') === String(subjectId));
    const list = en.map(e => e.student).filter(Boolean);
    return list.length ? list : students; // fallback: whole roster if no enrollment data
  }

  async function buildBulkGrid() {
    const subjectId = $('#bSubject').value, examId = $('#bExam').value;
    const grid = $('#bulkGrid');
    $('#bulkStatus').textContent = '';
    const exam = exams.find(x => String(x.examId) === examId);
    if (!subjectId || !examId) {
      grid.innerHTML = '<div class="state"><div class="big">👆</div><div class="msg">Pick a subject and an exam</div><div class="sub">The enrolled class will appear here.</div></div>';
      $('#bulkSaveBtn').disabled = true;
      return;
    }
    const max = exam ? exam.totalMarks : 100;
    const roster = enrolledFor(subjectId);

    /* prefill existing records for this subject+exam so duplicates are visible */
    let existing = [];
    try {
      existing = await api('/api/marks/search' + qs({ subjectId, examId }));
    } catch (e) { existing = []; }

    grid.innerHTML =
      '<div class="table-wrap" style="max-height:300px;overflow-y:auto"><table class="data"><thead><tr>' +
      '<th>Student</th><th>Roll No</th><th style="width:140px">Marks (max ' + max + ')</th>' +
      '</tr></thead><tbody>' +
      roster.map(st => {
        const rec = existing.find(m => m.student && String(m.student.studentId) === String(st.studentId));
        return '<tr data-id="' + st.studentId + '"' + (rec ? ' data-existing="' + rec.marksId + '"' : '') + '><td>' + esc(fullName(st)) + '</td><td>' + esc(st.rollNumber) + '</td>' +
          '<td><input type="number" step="0.5" min="0" max="' + max + '" class="bm" data-id="' + st.studentId + '" ' +
          'style="width:100%;padding:6px 9px;border:1px solid var(--border);border-radius:7px" placeholder="—" value="' + (rec ? rec.marksObtained : '') + '"></td></tr>';
      }).join('') +
      '</tbody></table></div>';

    $('#bulkStatus').textContent = existing.length
      ? existing.length + ' record(s) already exist for this subject & exam (prefilled). Saving updates them instead of duplicating; empty rows are skipped.'
      : roster.length + ' enrolled student(s). Empty rows are skipped.';
    $('#bulkSaveBtn').disabled = false;
  }
  $('#bSubject').addEventListener('change', buildBulkGrid);
  $('#bExam').addEventListener('change', buildBulkGrid);

  $('#bulkBtn').onclick = () => {
    if (preselectedSubject) { $('#bSubject').value = preselectedSubject; sessionStorage.removeItem('spis_subject'); }
    else { $('#bSubject').value = ''; }
    $('#bExam').value = '';
    buildBulkGrid();
    openModal('bulkModal');
  };

  $('#bulkSaveBtn').onclick = async () => {
    const subjectId = Number($('#bSubject').value);
    const examId = Number($('#bExam').value);
    const entries = $all('#bulkGrid .bm')
      .filter(inp => inp.value !== '')
      .map(inp => ({
        studentId: Number(inp.dataset.id),
        marks: Number(inp.value),
        marksId: inp.closest('tr').dataset.existing || null
      }));
    if (!entries.length) { $('#bulkErr').textContent = 'Enter marks for at least one student.'; return; }
    $('#bulkErr').textContent = '';

    await withBusy($('#bulkSaveBtn'), 'Saving…', async () => {
      let ok = 0, fail = 0, lastErr = '';
      for (const en of entries) {
        try {
          // Always POST: the backend's duplicate guard (student+subject+exam → 409)
          // protects against double entries even if the grid's prefill went stale.
          await api('/api/marks', {
            method: 'POST',
            body: JSON.stringify({
              marksObtained: en.marks,
              student: { studentId: en.studentId },
              subject: { subjectId },
              exam: { examId }
            })
          });
          ok++;
        } catch (e) {
          if (e.status === 409) { fail++; lastErr = 'One of the rows already exists — use Edit on the record in the table below.'; }
          else { fail++; lastErr = e.message; }
        }
      }
      toast(ok + ' record(s) saved' + (fail ? ', ' + fail + ' skipped/failed. ' + lastErr : '.') + '.', fail ? 'warn' : 'success', 'Class entry');
      closeModal('bulkModal');
      load();
    });
  };

  /* ---------- export ---------- */
  $('#exportBtn').onclick = async () => {
    if (!allRows.length) { toast('Nothing to export yet.', 'warn'); return; }
    const rows = allRows.map(m => ({
      student: fullName(m.student), roll: m.student ? m.student.rollNumber : '',
      subject: m.subject ? m.subject.subjectName : '', exam: m.exam ? m.exam.examName : '',
      marks: m.marksObtained
    }));
    await exportWithPicker('marks', [
      { label: 'Student', value: 'student' }, { label: 'Roll No', value: 'roll' },
      { label: 'Subject', value: 'subject' }, { label: 'Exam', value: 'exam' }, { label: 'Marks', value: 'marks' }
    ], rows);
  };

  await loadLookups();
  await load();
};
