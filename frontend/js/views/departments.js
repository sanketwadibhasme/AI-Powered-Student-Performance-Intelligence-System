/* Departments module — Admin: CRUD + live performance stats from department report */
window.view_departments = async function (view) {
  let allRows = [];

  view.innerHTML =
    '<div class="page-head"><div><h2>Departments</h2><div class="desc">Manage departments and monitor their performance.</div></div>' +
    '<div class="page-actions"><button class="btn ghost" id="exportBtn">⬇ Export</button>' +
    '<button class="btn primary" id="addBtn">＋ Add Department</button></div></div>' +

    '<div class="kpis" id="kpis"></div>' +

    '<div class="card"><div class="card-body">' +
    '<div class="table-wrap"><table class="data"><thead><tr>' +
    '<th>Code</th><th>Department</th><th>HOD</th><th>Students</th><th>Faculty</th><th>Subjects</th><th>Avg Marks</th><th>Attendance</th><th>Actions</th>' +
    '</tr></thead><tbody id="rows"></tbody></table></div>' +
    '</div></div>';

  view.insertAdjacentHTML('beforeend',
    '<div class="modal-backdrop" id="deptModal"><div class="modal">' +
    '<div class="modal-head"><h3 id="dmTitle">Add Department</h3><button class="x" data-close>✕</button></div>' +
    '<div class="modal-body"><form id="deptForm"><div class="form-grid">' +
    '<div class="field"><label>Department Code <span class="req">*</span></label><input name="departmentCode" required maxlength="10" placeholder="e.g. CSE" style="text-transform:uppercase">' +
    '<div class="help">2–10 uppercase letters, unique</div></div>' +
    '<div class="field"><label>Department Name <span class="req">*</span></label><input name="departmentName" required minlength="2" maxlength="100"></div>' +
    '<div class="field full"><label>HOD Name <span class="req">*</span></label><input name="hodName" required minlength="3" maxlength="100"></div>' +
    '</div><div class="form-error"></div>' +
    '<div class="form-foot"><button type="button" class="btn ghost" data-close>Cancel</button>' +
    '<button type="submit" class="btn primary" id="saveBtn">Save Department</button></div>' +
    '</form></div></div></div>');

  initModals();

  async function load() {
    const tbody = $('#rows');
    renderLoading(tbody, 9);
    try {
      const [depts, perf] = await Promise.all([api('/api/departments'), api('/api/reports/departments')]);
      const perfById = {};
      perf.forEach(p => { perfById[p.departmentId] = p; });
      allRows = depts;

      $('#kpis').innerHTML = [
        ['Total Departments', depts.length, 'blue'],
        ['Total Students', perf.reduce((s, p) => s + (p.totalStudents || 0), 0), 'green'],
        ['Total Faculty', perf.reduce((s, p) => s + (p.totalFaculty || 0), 0), 'violet'],
        ['Total Subjects', perf.reduce((s, p) => s + (p.totalSubjects || 0), 0), 'amber']
      ].map(k => '<div class="kpi ' + k[2] + '"><div class="label">' + k[0] + '</div><div class="value">' + k[1] + '</div></div>').join('');

      if (!depts.length) {
        renderEmpty(tbody, 9, 'No departments yet', 'Create your first department to get started.');
      } else {
        tbody.innerHTML = depts.map(d => {
          const p = perfById[d.departmentId] || {};
          return '<tr data-id="' + d.departmentId + '">' +
            '<td><span class="badge blue">' + esc(d.departmentCode) + '</span></td>' +
            '<td><b>' + esc(d.departmentName) + '</b></td>' +
            '<td>' + esc(d.hodName || '—') + '</td>' +
            '<td>' + (p.totalStudents ?? '—') + '</td>' +
            '<td>' + (p.totalFaculty ?? '—') + '</td>' +
            '<td>' + (p.totalSubjects ?? '—') + '</td>' +
            '<td>' + marksBadge(p.averageMarks) + '</td>' +
            '<td>' + attBadge(p.averageAttendance) + '</td>' +
            '<td class="actions">' +
            '<button class="btn ghost sm" data-act="edit">Edit</button>' +
            '<button class="btn danger sm" data-act="del">Delete</button>' +
            '</td></tr>';
        }).join('');
      }
    } catch (err) { renderError(tbody, 9, err, load); }
  }

  $('#addBtn').onclick = () => {
    const form = $('#deptForm');
    form.reset(); clearFormErrors(form);
    $('#dmTitle').textContent = 'Add Department';
    form.dataset.id = '';
    openModal('deptModal');
  };

  $('#deptForm').addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.target;
    const json = formToJSON(form);
    json.departmentCode = json.departmentCode.toUpperCase();
    const id = form.dataset.id;
    try {
      if (id) { await api('/api/departments/' + id, { method: 'PUT', body: JSON.stringify(json) }); toast('Department updated successfully.'); }
      else { await api('/api/departments', { method: 'POST', body: JSON.stringify(json) }); toast('Department created successfully.'); }
      closeModal('deptModal');
      load();
    } catch (err) { showFormErrors(form, err.message); }
  });

  $('#rows').addEventListener('click', async e => {
    const btn = e.target.closest('button[data-act]');
    if (!btn) return;
    const id = btn.closest('tr').dataset.id;
    if (btn.dataset.act === 'edit') {
      const d = await api('/api/departments/' + id);
      const form = $('#deptForm');
      form.reset(); clearFormErrors(form);
      $('#dmTitle').textContent = 'Edit Department — ' + d.departmentCode;
      form.departmentCode.value = d.departmentCode;
      form.departmentName.value = d.departmentName;
      form.hodName.value = d.hodName || '';
      form.dataset.id = d.departmentId;
      openModal('deptModal');
    } else {
      const ok = await confirmDialog('Delete Department?', 'Are you sure you want to delete this department? Students, faculty and subjects linked to it may block deletion.');
      if (!ok) return;
      try { await api('/api/departments/' + id, { method: 'DELETE' }); toast('Department deleted successfully.'); load(); }
      catch (err) { toast(err.message, 'error'); }
    }
  });

  $('#exportBtn').onclick = async () => {
    if (!allRows.length) { toast('Nothing to export yet.', 'warn'); return; }
    const perf = await api('/api/reports/departments');
    const perfById = {}; perf.forEach(p => { perfById[p.departmentId] = p; });
    const rows = allRows.map(d => {
      const p = perfById[d.departmentId] || {};
      return {
        code: d.departmentCode, name: d.departmentName, hod: d.hodName || '',
        students: p.totalStudents ?? '', faculty: p.totalFaculty ?? '', subjects: p.totalSubjects ?? '',
        avgMarks: p.averageMarks != null ? Number(p.averageMarks.toFixed(1)) : '',
        attendance: p.averageAttendance != null ? Number(p.averageAttendance.toFixed(1)) : ''
      };
    });
    await exportWithPicker('departments', [
      { label: 'Code', value: 'code' }, { label: 'Department', value: 'name' }, { label: 'HOD', value: 'hod' },
      { label: 'Students', value: 'students' }, { label: 'Faculty', value: 'faculty' },
      { label: 'Subjects', value: 'subjects' }, { label: 'Avg Marks', value: 'avgMarks' },
      { label: 'Attendance %', value: 'attendance' }
    ], rows);
  };

  await load();
};
