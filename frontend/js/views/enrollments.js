/* Enrollments module — enroll students in subjects, bulk enroll, remove */
window.view_enrollments = async function (view) {
  let students = [], subjects = [], allRows = [];

  view.innerHTML =
    '<div class="page-head"><div><h2>Enrollments</h2><div class="desc">Enroll students in subjects for an academic year and semester.</div></div>' +
    '<div class="page-actions">' +
    '<button class="btn ghost" id="bulkBtn">⧉ Bulk Enroll</button>' +
    '<button class="btn primary" id="addBtn">＋ Enroll Student</button></div></div>' +

    '<div class="card"><div class="card-body">' +
    '<div class="toolbar">' +
    '  <select id="fStudent"><option value="">All students</option></select>' +
    '  <select id="fSubject"><option value="">All subjects</option></select>' +
    '  <select id="fSem"><option value="">All semesters</option>' + [1,2,3,4,5,6,7,8].map(s => '<option>' + s + '</option>').join('') + '</select>' +
    '  <input type="text" id="fYear" placeholder="Academic year 2025-2026" style="height:37px;border:1px solid var(--border);border-radius:8px;padding:0 10px;font-size:13px">' +
    '  <span class="count" id="countLbl"></span>' +
    '</div>' +
    '<div class="table-wrap"><table class="data"><thead><tr>' +
    '<th>Student</th><th>Roll No</th><th>Subject</th><th>Code</th><th>Semester</th><th>Academic Year</th><th>Actions</th>' +
    '</tr></thead><tbody id="rows"></tbody></table></div>' +
    '</div></div>';

  const enrollFormHTML = `
    <div class="modal-backdrop" id="enrModal"><div class="modal wide">
      <div class="modal-head"><h3 id="enrTitle">Enroll Student</h3><button class="x" data-close>✕</button></div>
      <div class="modal-body"><form id="enrForm"><div class="form-grid">
        <div class="field"><label>Student <span class="req">*</span></label><select name="studentId" required><option value="">Select…</option></select></div>
        <div class="field"><label>Subject <span class="req">*</span></label><select name="subjectId" required><option value="">Select…</option></select></div>
        <div class="field"><label>Semester <span class="req">*</span></label><select name="semester" data-type="number" required>${[1,2,3,4,5,6,7,8].map(s => '<option>' + s + '</option>').join('')}</select></div>
        <div class="field"><label>Academic Year <span class="req">*</span></label><input name="academicYear" required pattern="\\d{4}-\\d{4}" placeholder="2025-2026"></div>
      </div><div class="form-error"></div>
      <div class="form-foot"><button type="button" class="btn ghost" data-close>Cancel</button>
      <button type="submit" class="btn primary" id="saveBtn">Enroll</button></div>
      </form></div></div></div>

    <div class="modal-backdrop" id="bulkModal"><div class="modal wide">
      <div class="modal-head"><h3>Bulk Enroll</h3><button class="x" data-close>✕</button></div>
      <div class="modal-body"><form id="bulkForm">
        <div class="form-grid">
          <div class="field"><label>Subject <span class="req">*</span></label><select name="subjectId" id="bulkSubject" required><option value="">Select…</option></select></div>
          <div class="field"><label>Semester <span class="req">*</span></label><select name="semester" id="bulkSem" required>${[1,2,3,4,5,6,7,8].map(s => '<option>' + s + '</option>').join('')}</select></div>
          <div class="field full"><label>Academic Year <span class="req">*</span></label><input name="academicYear" id="bulkYear" required pattern="\\d{4}-\\d{4}" placeholder="2025-2026"></div>
        </div>
        <div class="field mt"><label>Students <span class="req">*</span> <span class="help" style="display:inline">pick from the list; use Ctrl/Cmd-click for multiple</span></label>
        <select id="bulkStudents" multiple size="9" style="width:100%;padding:8px;border:1px solid var(--border);border-radius:8px"></select></div>
        <div class="form-error"></div>
        <div class="form-foot"><button type="button" class="btn ghost" data-close>Cancel</button>
        <button type="submit" class="btn primary" id="bulkSaveBtn">Enroll Selected</button></div>
      </form></div></div></div>`;
  view.insertAdjacentHTML('beforeend', enrollFormHTML);
  initModals();

  async function loadLookups() {
    [students, subjects] = await Promise.all([api('/api/students'), api('/api/subjects')]);
    fillSelect($('#fStudent'), students, 'studentId', s => fullName(s) + ' (' + s.rollNumber + ')', 'All students');
    fillSelect($('#fSubject'), subjects, 'subjectId', s => s.subjectName + ' (' + s.subjectCode + ')', 'All subjects');
    fillSelect($('#enrForm [name=studentId]'), students, 'studentId', s => fullName(s) + ' (' + s.rollNumber + ')', 'Select…');
    fillSelect($('#enrForm [name=subjectId]'), subjects, 'subjectId', s => s.subjectName + ' (' + s.subjectCode + ')', 'Select…');
    fillSelect($('#bulkSubject'), subjects, 'subjectId', s => s.subjectName + ' (' + s.subjectCode + ')', 'Select…');
    $('#bulkStudents').innerHTML = students.map(s =>
      '<option value="' + s.studentId + '">' + esc(fullName(s) + ' (' + s.rollNumber + ') · Sem ' + s.currentSemester) + '</option>').join('');

    // quick-fill: choosing a student pre-fills their current semester
    $('#enrForm [name=studentId]').addEventListener('change', e => {
      const st = students.find(x => String(x.studentId) === e.target.value);
      if (st) $('#enrForm [name=semester]').value = st.currentSemester;
    });
  }

  async function load() {
    const tbody = $('#rows');
    renderLoading(tbody, 7);
    try {
      const params = {
        studentId: $('#fStudent').value || undefined,
        subjectId: $('#fSubject').value || undefined,
        semester: $('#fSem').value || undefined,
        academicYear: $('#fYear').value.trim() || undefined
      };
      allRows = await api('/api/enrollments/search' + qs(params));
      $('#countLbl').textContent = allRows.length + ' enrollment(s)';

      if (!allRows.length) renderEmpty(tbody, 7, 'No enrollments found', 'Adjust filters or enroll a student.');
      else tbody.innerHTML = allRows.map(en =>
        '<tr data-id="' + en.enrollmentId + '">' +
        '<td><b>' + esc(fullName(en.student)) + '</b></td>' +
        '<td>' + esc(en.student ? en.student.rollNumber : '—') + '</td>' +
        '<td>' + esc(en.subject ? en.subject.subjectName : '—') + '</td>' +
        '<td><span class="badge violet">' + esc(en.subject ? en.subject.subjectCode : '—') + '</span></td>' +
        '<td>' + en.semester + '</td>' +
        '<td>' + esc(en.academicYear) + '</td>' +
        '<td class="actions"><button class="btn danger sm" data-act="del">Remove</button></td>' +
        '</tr>').join('');
    } catch (err) { renderError(tbody, 7, err, load); }
  }

  ['#fStudent', '#fSubject', '#fSem'].forEach(sel => $(sel).addEventListener('change', load));
  $('#fYear').addEventListener('input', () => {
    clearTimeout(window.__enrDeb); window.__enrDeb = setTimeout(load, 350);
  });

  $('#addBtn').onclick = () => {
    const form = $('#enrForm');
    form.reset(); clearFormErrors(form);
    $('#enrTitle').textContent = 'Enroll Student';
    openModal('enrModal');
  };

  $('#enrForm').addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.target;
    const json = coerceJSON(form, formToJSON(form));
    const payload = {
      academicYear: json.academicYear, semester: json.semester,
      student: { studentId: Number(json.studentId) },
      subject: { subjectId: Number(json.subjectId) }
    };
    try {
      await api('/api/enrollments', { method: 'POST', body: JSON.stringify(payload) });
      toast('Student enrolled successfully.');
      closeModal('enrModal');
      load();
    } catch (err) { showFormErrors(form, err.message); }
  });

  $('#bulkBtn').onclick = () => {
    const form = $('#bulkForm');
    form.reset(); clearFormErrors(form);
    openModal('bulkModal');
  };

  $('#bulkForm').addEventListener('submit', async e => {
    e.preventDefault();
    const btn = $('#bulkSaveBtn');
    const subjectId = Number($('#bulkSubject').value);
    const semester = Number($('#bulkSem').value);
    const year = $('#bulkYear').value.trim();
    const ids = $all('#bulkStudents option:checked').map(o => Number(o.value));
    if (!subjectId || !semester || !year || !ids.length) {
      showFormErrors($('#bulkForm'), 'Select a subject, semester, academic year and at least one student.');
      return;
    }
    await withBusy(btn, 'Enrolling…', async () => {
      let ok = 0, fail = 0;
      for (const sid of ids) {
        try {
          await api('/api/enrollments', {
            method: 'POST',
            body: JSON.stringify({ academicYear: year, semester, student: { studentId: sid }, subject: { subjectId } })
          });
          ok++;
        } catch (e) { fail++; }
      }
      toast(ok + ' enrolled' + (fail ? ', ' + fail + ' failed (already enrolled?)' : '') + '.', fail ? 'warn' : 'success');
      closeModal('bulkModal');
      load();
    });
  });

  $('#rows').addEventListener('click', async e => {
    const btn = e.target.closest('button[data-act]');
    if (!btn) return;
    const id = btn.closest('tr').dataset.id;
    const ok = await confirmDialog('Remove Enrollment?', 'Remove this student from the subject? Their marks/attendance records are not deleted.');
    if (!ok) return;
    try { await api('/api/enrollments/' + id, { method: 'DELETE' }); toast('Enrollment removed.'); load(); }
    catch (err) { toast(err.message, 'error'); }
  });

  await loadLookups();
  await load();
};
