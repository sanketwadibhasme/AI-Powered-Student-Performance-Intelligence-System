/* Faculty module — Admin: CRUD + department filter + teaching load (from subjects) */
window.view_faculty = async function (view) {
  const state = { sortKey: 'facultyName', sortDir: 1 };
  let departments = [], subjects = [], allRows = [];

  view.innerHTML =
    '<div class="page-head"><div><h2>Faculty</h2><div class="desc">Manage faculty members and their department/subject assignments.</div></div>' +
    '<div class="page-actions"><button class="btn ghost" id="exportBtn">⬇ Export</button>' +
    '<button class="btn primary" id="addBtn">＋ Add Faculty</button></div></div>' +

    '<div class="card"><div class="card-body">' +
    '<div class="toolbar">' +
    '  <div class="search">🔍<input id="q" placeholder="Search name / employee code / email…"></div>' +
    '  <select id="fDept"><option value="">All departments</option></select>' +
    '  <span class="count" id="countLbl"></span>' +
    '</div>' +
    '<div class="table-wrap"><table class="data"><thead><tr>' +
    '<th class="sortable" data-k="employeeCode">Emp Code ⇅</th>' +
    '<th class="sortable" data-k="facultyName">Name ⇅</th>' +
    '<th>Email</th><th>Phone</th><th>Designation</th><th>Department</th><th>Subjects</th><th>Actions</th>' +
    '</tr></thead><tbody id="rows"></tbody></table></div>' +
    '<div class="table-note muted">Teaching load is derived from subject assignments (no separate workload API exists).</div>' +
    '</div></div>';

  view.insertAdjacentHTML('beforeend',
    '<div class="modal-backdrop" id="facModal"><div class="modal wide">' +
    '<div class="modal-head"><h3 id="fmTitle">Add Faculty</h3><button class="x" data-close>✕</button></div>' +
    '<div class="modal-body"><form id="facForm"><div class="form-grid">' +
    '<div class="field"><label>Employee Code <span class="req">*</span></label><input name="employeeCode" required></div>' +
    '<div class="field"><label>Full Name <span class="req">*</span></label><input name="facultyName" required minlength="2" maxlength="50"></div>' +
    '<div class="field"><label>Email <span class="req">*</span></label><input name="email" type="email" required></div>' +
    '<div class="field"><label>Phone (10 digits) <span class="req">*</span></label><input name="phone" required pattern="[0-9]{10}"></div>' +
    '<div class="field"><label>Designation <span class="req">*</span></label><input name="designation" required></div>' +
    '<div class="field"><label>Department <span class="req">*</span></label><select name="departmentId" required><option value="">Select…</option></select></div>' +
    '</div><div class="form-error"></div>' +
    '<div class="form-foot"><button type="button" class="btn ghost" data-close>Cancel</button>' +
    '<button type="submit" class="btn primary" id="saveBtn">Save Faculty</button></div>' +
    '</form></div></div></div>');

  initModals();

  async function loadLookups() {
    [departments, subjects] = await Promise.all([api('/api/departments'), api('/api/subjects')]);
    fillSelect($('#fDept'), departments, 'departmentId', d => d.departmentName, 'All departments');
    fillSelect($('#facForm [name=departmentId]'), departments, 'departmentId', d => d.departmentName + ' (' + d.departmentCode + ')', 'Select…');
  }

  async function load() {
    const tbody = $('#rows');
    renderLoading(tbody, 8);
    try {
      const q = ($('#q').value || '').trim().toLowerCase();
      const deptId = $('#fDept').value;
      // use backend search when filters are set, otherwise the plain list
      allRows = (q || deptId)
        ? await api('/api/faculties/search' + qs({ facultyName: q || undefined, departmentId: deptId || undefined }))
        : await api('/api/faculties');

      // client-side refinement across all fields
      if (q) allRows = allRows.filter(f =>
        (f.facultyName || '').toLowerCase().includes(q) ||
        (f.employeeCode || '').toLowerCase().includes(q) ||
        (f.email || '').toLowerCase().includes(q));

      allRows.sort((a, b) => {
        const va = a[state.sortKey], vb = b[state.sortKey];
        if (va == null) return 1; if (vb == null) return -1;
        return String(va).localeCompare(String(vb)) * state.sortDir;
      });

      $('#countLbl').textContent = allRows.length + ' faculty';
      if (!allRows.length) {
        renderEmpty(tbody, 8, 'No faculty found', 'Adjust filters or add a faculty member.');
      } else {
        tbody.innerHTML = allRows.map(f => {
          const load = subjects.filter(s => s.faculty && s.faculty.facultyId === f.facultyId);
          return '<tr data-id="' + f.facultyId + '">' +
            '<td><b>' + esc(f.employeeCode) + '</b></td>' +
            '<td>' + esc(f.facultyName) + '</td>' +
            '<td class="muted">' + esc(f.email) + '</td>' +
            '<td>' + esc(f.phone) + '</td>' +
            '<td>' + esc(f.designation) + '</td>' +
            '<td>' + esc(f.department ? f.department.departmentName : '—') + '</td>' +
            '<td>' + (load.length ? load.map(s => '<span class="badge blue" style="margin:1px">' + esc(s.subjectCode) + '</span>').join(' ') : '<span class="muted">None</span>') + '</td>' +
            '<td class="actions">' +
            '<button class="btn ghost sm" data-act="edit">Edit</button>' +
            '<button class="btn danger sm" data-act="del">Delete</button>' +
            '</td></tr>';
        }).join('');
      }
    } catch (err) { renderError(tbody, 8, err, load); }
  }

  $('#q').addEventListener('input', () => { state.page = 0; load(); });
  $('#fDept').addEventListener('change', load);
  $all('th.sortable').forEach(th => th.addEventListener('click', () => {
    const k = th.dataset.k;
    state.sortDir = (state.sortKey === k) ? -state.sortDir : 1;
    state.sortKey = k; load();
  }));

  function openForm(f) {
    const form = $('#facForm');
    form.reset(); clearFormErrors(form);
    $('#fmTitle').textContent = f ? 'Edit Faculty — ' + f.employeeCode : 'Add Faculty';
    if (f) {
      form.employeeCode.value = f.employeeCode;
      form.facultyName.value = f.facultyName;
      form.email.value = f.email;
      form.phone.value = f.phone;
      form.designation.value = f.designation;
      form.departmentId.value = f.department ? f.department.departmentId : '';
    }
    form.dataset.id = f ? f.facultyId : '';
    openModal('facModal');
  }

  $('#addBtn').onclick = () => openForm(null);

  $('#facForm').addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.target;
    const json = formToJSON(form);
    const payload = Object.assign({}, json, { department: { departmentId: Number(json.departmentId) } });
    delete payload.departmentId;
    const id = form.dataset.id;
    try {
      if (id) { await api('/api/faculties/' + id, { method: 'PUT', body: JSON.stringify(payload) }); toast('Faculty updated successfully.'); }
      else { await api('/api/faculties', { method: 'POST', body: JSON.stringify(payload) }); toast('Faculty created successfully.'); }
      closeModal('facModal');
      load();
    } catch (err) { showFormErrors(form, err.message); }
  });

  $('#rows').addEventListener('click', async e => {
    const btn = e.target.closest('button[data-act]');
    if (!btn) return;
    const id = btn.closest('tr').dataset.id;
    if (btn.dataset.act === 'edit') {
      openForm(await api('/api/faculties/' + id));
    } else {
      const ok = await confirmDialog('Delete Faculty?', 'Are you sure you want to delete this faculty member? Subjects assigned to them may need reassignment.');
      if (!ok) return;
      try { await api('/api/faculties/' + id, { method: 'DELETE' }); toast('Faculty deleted successfully.'); load(); }
      catch (err) { toast(err.message, 'error'); }
    }
  });

  $('#exportBtn').onclick = async () => {
    if (!allRows.length) { toast('Nothing to export yet.', 'warn'); return; }
    const rows = allRows.map(f => ({
      employeeCode: f.employeeCode, facultyName: f.facultyName, email: f.email, phone: f.phone,
      designation: f.designation, dept: f.department ? f.department.departmentName : '',
      subjects: subjects.filter(s => s.faculty && s.faculty.facultyId === f.facultyId).map(s => s.subjectCode).join(' | ')
    }));
    await exportWithPicker('faculty', [
      { label: 'Emp Code', value: 'employeeCode' }, { label: 'Name', value: 'facultyName' },
      { label: 'Email', value: 'email' }, { label: 'Phone', value: 'phone' },
      { label: 'Designation', value: 'designation' }, { label: 'Department', value: 'dept' },
      { label: 'Subjects', value: 'subjects' }
    ], rows);
  };

  await loadLookups();
  await load();
};
