/* Students module — list + search + filters + sort + pagination + CRUD */
window.view_students = async function (view) {
  const role = getRole();
  const state = { page: 0, size: 10, sortKey: 'rollNumber', sortDir: 1, filters: {} };
  let departments = [];
  let allRows = null; // search results cached for client sort/paginate

  view.innerHTML =
    '<div class="page-head"><div><h2>Students</h2><div class="desc">Search, filter and manage student records.</div></div>' +
    '<div class="page-actions"><button class="btn ghost" id="exportBtn">⬇ Export</button>' +
    '<button class="btn primary" id="addBtn">＋ Add Student</button></div></div>' +

    '<div class="card"><div class="card-body">' +
    '<div class="toolbar">' +
    '  <div class="search">🔍<input id="qName" placeholder="Search name / roll number…"></div>' +
    '  <select id="fDept"><option value="">All departments</option></select>' +
    '  <select id="fSem"><option value="">All semesters</option>' + [1,2,3,4,5,6,7,8].map(s => '<option>' + s + '</option>').join('') + '</select>' +
    '  <select id="fStatus"><option value="">All statuses</option>' +
    '    <option>ACTIVE</option><option>GRADUATED</option><option>SUSPENDED</option><option>DROPPED</option></select>' +
    '  <span class="count" id="countLbl"></span>' +
    '</div>' +
    '<div class="table-wrap"><table class="data"><thead><tr>' +
    '<th class="sortable" data-k="rollNumber">Roll No ⇅</th>' +
    '<th class="sortable" data-k="name">Name ⇅</th>' +
    '<th>Email</th><th>Department</th>' +
    '<th class="sortable" data-k="currentSemester">Sem ⇅</th>' +
    '<th>Status</th>' +
    '<th class="sortable" data-k="attendance">Attendance ⇅</th>' +
    '<th class="sortable" data-k="avgMarks">Avg Marks ⇅</th>' +
    '<th>Actions</th>' +
    '</tr></thead><tbody id="rows"></tbody></table></div>' +
    '<div class="pager"><button id="prevBtn">← Prev</button><span id="pageInfo"></span><button id="nextBtn">Next →</button><span class="grow"></span>' +
    '<span class="muted">Sort by clicking column headers</span></div>' +
    '</div></div>';

  /* ---------- modals ---------- */
  view.insertAdjacentHTML('beforeend',
    '<div class="modal-backdrop" id="studentModal"><div class="modal wide">' +
    '<div class="modal-head"><h3 id="smTitle">Add Student</h3><button class="x" data-close>✕</button></div>' +
    '<div class="modal-body"><form id="studentForm"><div class="form-grid">' +
    '<div class="field"><label>Roll Number <span class="req">*</span></label><input name="rollNumber" required><div class="err">Required</div></div>' +
    '<div class="field"><label>Department <span class="req">*</span></label><select name="departmentId" required><option value="">Select…</option></select></div>' +
    '<div class="field"><label>First Name <span class="req">*</span></label><input name="firstName" required minlength="2" maxlength="20"></div>' +
    '<div class="field"><label>Last Name <span class="req">*</span></label><input name="lastName" required minlength="2" maxlength="20"></div>' +
    '<div class="field"><label>Email <span class="req">*</span></label><input name="email" type="email" required></div>' +
    '<div class="field"><label>Phone (10 digits) <span class="req">*</span></label><input name="phone" required pattern="[0-9]{10}"></div>' +
    '<div class="field"><label>Gender <span class="req">*</span></label><select name="gender" required><option>MALE</option><option>FEMALE</option><option>OTHER</option></select></div>' +
    '<div class="field"><label>Date of Birth <span class="req">*</span></label><input name="dob" type="date" required></div>' +
    '<div class="field"><label>Admission Year <span class="req">*</span></label><input name="admissionYear" data-type="number" type="number" min="2000" max="2035" required></div>' +
    '<div class="field"><label>Current Semester <span class="req">*</span></label><select name="currentSemester" data-type="number" required>' + [1,2,3,4,5,6,7,8].map(s => '<option>' + s + '</option>').join('') + '</select></div>' +
    '<div class="field full"><label>Status <span class="req">*</span></label><select name="status" required><option>ACTIVE</option><option>GRADUATED</option><option>SUSPENDED</option><option>DROPPED</option></select></div>' +
    '</div><div class="form-error"></div>' +
    '<div class="form-foot"><button type="button" class="btn ghost" data-close>Cancel</button>' +
    '<button type="submit" class="btn primary" id="saveBtn">Save Student</button></div>' +
    '</form></div></div></div>');

  initModals();

  /* ---------- data ---------- */
  async function loadLookups() {
    departments = await api('/api/departments');
    fillSelect($('#fDept'), departments, 'departmentId', d => d.departmentName, 'All departments');
    fillSelect($('#studentForm [name=departmentId]'), departments, 'departmentId', d => d.departmentName + ' (' + d.departmentCode + ')', 'Select…');
  }

  async function perfFor(list) {
    // join attendance/marks summaries from the student performance report (already computed server-side)
    try {
      const report = await api('/api/reports/students');
      const byId = {};
      report.forEach(r => { byId[r.studentId] = r; });
      list.forEach(st => {
        const r = byId[st.studentId];
        st.avgMarks = r ? r.averageMarks : null;
        st.attendance = r ? r.attendancePercentage : null;
      });
    } catch (e) { /* summaries optional */ }
    return list;
  }

  async function load() {
    const tbody = $('#rows');
    renderLoading(tbody, 9);
    try {
      let list;
      const hasFilters = Object.values(state.filters).some(v => v);
      if (hasFilters) {
        list = await api('/api/students/search' + qs(state.filters));
      } else {
        list = await api('/api/students');
      }
      await perfFor(list);

      // client-side text search (name or roll)
      const q = ($('#qName').value || '').trim().toLowerCase();
      if (q) list = list.filter(st =>
        (st.firstName + ' ' + st.lastName).toLowerCase().includes(q) ||
        (st.rollNumber || '').toLowerCase().includes(q));

      // sort
      const key = state.sortKey;
      list.sort((a, b) => {
        const va = key === 'name' ? (a.lastName || '') : a[key];
        const vb = key === 'name' ? (b.lastName || '') : b[key];
        if (va == null) return 1; if (vb == null) return -1;
        if (typeof va === 'number') return (va - vb) * state.sortDir;
        return String(va).localeCompare(String(vb)) * state.sortDir;
      });

      allRows = list;
      const pg = paginate(list, state.page, state.size);
      $('#countLbl').textContent = list.length + ' student(s)';
      if (!pg.content.length) {
        renderEmpty(tbody, 9, 'No students found', 'Try changing your filters or add a new student.');
      } else {
        tbody.innerHTML = pg.content.map(st =>
          '<tr data-id="' + st.studentId + '" data-roll="' + esc(st.rollNumber) + '">' +
          '<td><b>' + esc(st.rollNumber) + '</b></td>' +
          '<td>' + esc(fullName(st)) + '</td>' +
          '<td class="muted">' + esc(st.email) + '</td>' +
          '<td>' + esc(st.department ? st.department.departmentName : '—') + '</td>' +
          '<td>' + st.currentSemester + '</td>' +
          '<td>' + statusBadge(st.status) + '</td>' +
          '<td>' + attBadge(st.attendance) + '</td>' +
          '<td>' + marksBadge(st.avgMarks) + '</td>' +
          '<td class="actions">' +
          '<button class="btn ghost sm" data-act="view">View</button>' +
          '<button class="btn ghost sm" data-act="edit">Edit</button>' +
          '<button class="btn danger sm" data-act="del">Delete</button>' +
          '</td></tr>').join('');
      }
      bindPager(state, pg, load);
    } catch (err) {
      renderError(tbody, 9, err, load);
    }
  }

  /* ---------- events ---------- */
  let debounce;
  $('#qName').addEventListener('input', () => { clearTimeout(debounce); debounce = setTimeout(() => { state.page = 0; load(); }, 300); });
  ['#fDept', '#fSem', '#fStatus'].forEach(sel => {
    $(sel).addEventListener('change', () => {
      state.filters = {
        departmentId: $('#fDept').value,
        semester: $('#fSem').value,
        status: $('#fStatus').value
      };
      state.page = 0; load();
    });
  });
  $all('th.sortable').forEach(th => th.addEventListener('click', () => {
    const k = th.dataset.k;
    state.sortDir = (state.sortKey === k) ? -state.sortDir : 1;
    state.sortKey = k; load();
  }));

  /* ---------- CRUD ---------- */
  function openForm(student) {
    const form = $('#studentForm');
    form.reset();
    clearFormErrors(form);
    $('#smTitle').textContent = student ? 'Edit Student — ' + student.rollNumber : 'Add Student';
    if (student) {
      form.rollNumber.value = student.rollNumber;
      form.firstName.value = student.firstName;
      form.lastName.value = student.lastName;
      form.email.value = student.email;
      form.phone.value = student.phone;
      form.gender.value = student.gender;
      form.dob.value = student.dob;
      form.admissionYear.value = student.admissionYear;
      form.currentSemester.value = student.currentSemester;
      form.status.value = student.status;
      form.departmentId.value = student.department ? student.department.departmentId : '';
    }
    form.dataset.id = student ? student.studentId : '';
    openModal('studentModal');
  }

  $('#addBtn').onclick = () => openForm(null);

  $('#studentForm').addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.target;
    const btn = $('#saveBtn');
    const json = coerceJSON(form, formToJSON(form));
    const payload = Object.assign({}, json, {
      department: { departmentId: Number(json.departmentId) }
    });
    delete payload.departmentId;
    const id = form.dataset.id;
    try {
      if (id) { await api('/api/students/' + id, { method: 'PUT', body: JSON.stringify(payload) }); toast('Student updated successfully.'); }
      else { await api('/api/students', { method: 'POST', body: JSON.stringify(payload) }); toast('Student created successfully.'); }
      closeModal('studentModal');
      load();
    } catch (err) {
      showFormErrors(form, err.message);
    }
  });

  $('#rows').addEventListener('click', async e => {
    const btn = e.target.closest('button[data-act]');
    if (!btn) return;
    const tr = btn.closest('tr');
    const id = tr.dataset.id;
    const roll = tr.dataset.roll;

    if (btn.dataset.act === 'view') {
      location.hash = '#student'; sessionStorage.setItem('spis_selectedStudent', id);
    } else if (btn.dataset.act === 'edit') {
      const st = await api('/api/students/' + id);
      openForm(st);
    } else if (btn.dataset.act === 'del') {
      const ok = await confirmDialog('Delete Student?', 'Are you sure you want to delete student ' + roll + '? This removes their marks, attendance and enrollment history.');
      if (!ok) return;
      try {
        await api('/api/students/' + id, { method: 'DELETE' });
        toast('Student deleted successfully.');
        load();
      } catch (err) { toast(err.message, 'error'); }
    }
  });

  /* ---------- export ---------- */
  $('#exportBtn').onclick = async () => {
    if (!allRows || !allRows.length) { toast('Nothing to export yet.', 'warn'); return; }
    const rows = allRows.map(st => ({
      rollNumber: st.rollNumber, name: fullName(st), email: st.email, phone: st.phone,
      dept: st.department ? st.department.departmentName : '', currentSemester: st.currentSemester,
      status: st.status, attendance: st.attendance != null ? Number(st.attendance.toFixed(1)) : '',
      avgMarks: st.avgMarks != null ? Number(st.avgMarks.toFixed(1)) : ''
    }));
    await exportWithPicker('students', [
      { label: 'Roll Number', value: 'rollNumber' },
      { label: 'Name', value: 'name' },
      { label: 'Email', value: 'email' },
      { label: 'Phone', value: 'phone' },
      { label: 'Department', value: 'dept' },
      { label: 'Semester', value: 'currentSemester' },
      { label: 'Status', value: 'status' },
      { label: 'Attendance %', value: 'attendance' },
      { label: 'Average Marks', value: 'avgMarks' }
    ], rows);
  };

  await loadLookups();
  await load();
};
