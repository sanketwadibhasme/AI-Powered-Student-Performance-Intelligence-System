/* Subjects module — CRUD + faculty/department assignment + enrolled students */
window.view_subjects = async function (view) {
  let departments = [], facultyList = [], allRows = [];

  view.innerHTML =
    '<div class="page-head"><div><h2>Subjects</h2><div class="desc">Manage subjects, credits, department and faculty assignments.</div></div>' +
    '<div class="page-actions"><button class="btn ghost" id="exportBtn">⬇ Export</button>' +
    '<button class="btn primary" id="addBtn">＋ Add Subject</button></div></div>' +

    '<div class="card"><div class="card-body">' +
    '<div class="toolbar">' +
    '  <div class="search">🔍<input id="q" placeholder="Search subject name / code…"></div>' +
    '  <select id="fDept"><option value="">All departments</option></select>' +
    '  <select id="fFac"><option value="">All faculty</option></select>' +
    '  <span class="count" id="countLbl"></span>' +
    '</div>' +
    '<div class="table-wrap"><table class="data"><thead><tr>' +
    '<th>Code</th><th>Subject</th><th>Credits</th><th>Department</th><th>Faculty</th><th>Enrolled</th><th>Actions</th>' +
    '</tr></thead><tbody id="rows"></tbody></table></div>' +
    '</div></div>';

  view.insertAdjacentHTML('beforeend',
    '<div class="modal-backdrop" id="subModal"><div class="modal wide">' +
    '<div class="modal-head"><h3 id="sumTitle">Add Subject</h3><button class="x" data-close>✕</button></div>' +
    '<div class="modal-body"><form id="subForm"><div class="form-grid">' +
    '<div class="field"><label>Subject Code <span class="req">*</span></label><input name="subjectCode" required minlength="2" maxlength="20"></div>' +
    '<div class="field"><label>Subject Name <span class="req">*</span></label><input name="subjectName" required minlength="2" maxlength="50"></div>' +
    '<div class="field"><label>Credits <span class="req">*</span></label><input name="credits" data-type="number" type="number" min="1" max="10" required></div>' +
    '<div class="field"><label>Department <span class="req">*</span></label><select name="departmentId" required><option value="">Select…</option></select></div>' +
    '<div class="field full"><label>Faculty <span class="req">*</span></label><select name="facultyId" required><option value="">Select…</option></select></div>' +
    '</div><div class="form-error"></div>' +
    '<div class="form-foot"><button type="button" class="btn ghost" data-close>Cancel</button>' +
    '<button type="submit" class="btn primary" id="saveBtn">Save Subject</button></div>' +
    '</form></div></div></div>' +

    '<div class="modal-backdrop" id="enrolledModal"><div class="modal">' +
    '<div class="modal-head"><h3 id="emTitle">Enrolled students</h3><button class="x" data-close>✕</button></div>' +
    '<div class="modal-body" id="enrolledBody" style="max-height:55vh;overflow-y:auto"></div>' +
    '</div></div>');

  initModals();

  async function loadLookups() {
    [departments, facultyList] = await Promise.all([api('/api/departments'), api('/api/faculties')]);
    fillSelect($('#fDept'), departments, 'departmentId', d => d.departmentName, 'All departments');
    fillSelect($('#fFac'), facultyList, 'facultyId', f => f.facultyName, 'All faculty');
    fillSelect($('#subForm [name=departmentId]'), departments, 'departmentId', d => d.departmentName + ' (' + d.departmentCode + ')', 'Select…');
    fillSelect($('#subForm [name=facultyId]'), facultyList, 'facultyId', f => f.facultyName + ' (' + f.employeeCode + ')', 'Select…');
  }

  async function load() {
    const tbody = $('#rows');
    renderLoading(tbody, 7);
    try {
      const q = ($('#q').value || '').trim().toLowerCase();
      const deptId = $('#fDept').value, facId = $('#fFac').value;
      allRows = (q || deptId || facId)
        ? await api('/api/subjects/search' + qs({ subjectName: q || undefined, departmentId: deptId || undefined, facultyId: facId || undefined }))
        : await api('/api/subjects');

      const enrollments = await api('/api/enrollments'); // for enrolled-count column
      const countBy = {};
      enrollments.forEach(en => {
        const sid = en.subject ? en.subject.subjectId : null;
        if (sid) countBy[sid] = (countBy[sid] || 0) + 1;
      });

      if (q) allRows = allRows.filter(s => (s.subjectName || '').toLowerCase().includes(q) || (s.subjectCode || '').toLowerCase().includes(q));
      $('#countLbl').textContent = allRows.length + ' subject(s)';

      if (!allRows.length) renderEmpty(tbody, 7, 'No subjects found', 'Adjust filters or add a subject.');
      else tbody.innerHTML = allRows.map(s =>
        '<tr data-id="' + s.subjectId + '">' +
        '<td><span class="badge violet">' + esc(s.subjectCode) + '</span></td>' +
        '<td><b>' + esc(s.subjectName) + '</b></td>' +
        '<td>' + s.credits + '</td>' +
        '<td>' + esc(s.department ? s.department.departmentName : '—') + '</td>' +
        '<td>' + esc(s.faculty ? s.faculty.facultyName : '—') + '</td>' +
        '<td><button class="btn ghost sm" data-act="enrolled">' + (countBy[s.subjectId] || 0) + ' 👥</button></td>' +
        '<td class="actions">' +
        '<button class="btn ghost sm" data-act="edit">Edit</button>' +
        '<button class="btn danger sm" data-act="del">Delete</button>' +
        '</td></tr>').join('');
    } catch (err) { renderError(tbody, 7, err, load); }
  }

  $('#q').addEventListener('input', load);
  $('#fDept').addEventListener('change', load);
  $('#fFac').addEventListener('change', load);

  function openForm(s) {
    const form = $('#subForm');
    form.reset(); clearFormErrors(form);
    $('#sumTitle').textContent = s ? 'Edit Subject — ' + s.subjectCode : 'Add Subject';
    if (s) {
      form.subjectCode.value = s.subjectCode;
      form.subjectName.value = s.subjectName;
      form.credits.value = s.credits;
      form.departmentId.value = s.department ? s.department.departmentId : '';
      form.facultyId.value = s.faculty ? s.faculty.facultyId : '';
    }
    form.dataset.id = s ? s.subjectId : '';
    openModal('subModal');
  }

  $('#addBtn').onclick = () => openForm(null);

  $('#subForm').addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.target;
    const json = coerceJSON(form, formToJSON(form));
    const payload = {
      subjectCode: json.subjectCode, subjectName: json.subjectName, credits: json.credits,
      department: { departmentId: Number(json.departmentId) },
      faculty: { facultyId: Number(json.facultyId) }
    };
    const id = form.dataset.id;
    try {
      if (id) { await api('/api/subjects/' + id, { method: 'PUT', body: JSON.stringify(payload) }); toast('Subject updated successfully.'); }
      else { await api('/api/subjects', { method: 'POST', body: JSON.stringify(payload) }); toast('Subject created successfully.'); }
      closeModal('subModal');
      load();
    } catch (err) { showFormErrors(form, err.message); }
  });

  $('#rows').addEventListener('click', async e => {
    const btn = e.target.closest('button[data-act]');
    if (!btn) return;
    const id = btn.closest('tr').dataset.id;

    if (btn.dataset.act === 'enrolled') {
      const all = await api('/api/enrollments/search' + qs({ subjectId: id }));
      const subject = allRows.find(s => String(s.subjectId) === id);
      $('#emTitle').textContent = 'Enrolled in ' + (subject ? subject.subjectName : 'subject') + ' (' + all.length + ')';
      $('#enrolledBody').innerHTML = all.length
        ? all.map(en => '<div class="list-tile"><div class="ico">🎓</div>' +
            '<div class="grow"><b>' + esc(fullName(en.student)) + '</b><span>' + esc(en.student ? en.student.rollNumber : '') + ' · Sem ' + en.semester + ' · ' + esc(en.academicYear) + '</span></div></div>').join('')
        : '<div class="state"><div class="big">📭</div><div class="msg">No students enrolled yet</div><div class="sub">Use the Enrollments module to add them.</div></div>';
      openModal('enrolledModal');
    } else if (btn.dataset.act === 'edit') {
      openForm(await api('/api/subjects/' + id));
    } else {
      const ok = await confirmDialog('Delete Subject?', 'Are you sure you want to delete this subject? Marks and attendance linked to it may block deletion.');
      if (!ok) return;
      try { await api('/api/subjects/' + id, { method: 'DELETE' }); toast('Subject deleted successfully.'); load(); }
      catch (err) { toast(err.message, 'error'); }
    }
  });

  $('#exportBtn').onclick = async () => {
    if (!allRows.length) { toast('Nothing to export yet.', 'warn'); return; }
    const rows = allRows.map(s => ({
      code: s.subjectCode, name: s.subjectName, credits: s.credits,
      dept: s.department ? s.department.departmentName : '', faculty: s.faculty ? s.faculty.facultyName : ''
    }));
    await exportWithPicker('subjects', [
      { label: 'Code', value: 'code' }, { label: 'Subject', value: 'name' }, { label: 'Credits', value: 'credits' },
      { label: 'Department', value: 'dept' }, { label: 'Faculty', value: 'faculty' }
    ], rows);
  };

  await loadLookups();
  await load();
};
