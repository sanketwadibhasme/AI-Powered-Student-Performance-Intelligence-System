/* Users module — Admin: accounts, roles, enable/disable, link student record */
window.view_users = async function (view) {
  let students = [];
  let allRows = [];

  view.innerHTML =
    '<div class="page-head"><div><h2>Users</h2><div class="desc">Manage login accounts, roles and student-record links.</div></div>' +
    '<div class="page-actions"><button class="btn primary" id="addBtn">＋ Add User</button></div></div>' +

    '<div class="card"><div class="card-body">' +
    '<div class="toolbar">' +
    '  <div class="search">🔍<input id="q" placeholder="Search username / email…"></div>' +
    '  <select id="fRole"><option value="">All roles</option><option>ADMIN</option><option>FACULTY</option><option>STUDENT</option></select>' +
    '  <span class="count" id="countLbl"></span>' +
    '</div>' +
    '<div class="table-wrap"><table class="data"><thead><tr>' +
    '<th>ID</th><th>Username</th><th>Email</th><th>Role</th><th>Linked Student</th><th>Status</th><th>Actions</th>' +
    '</tr></thead><tbody id="rows"></tbody></table></div>' +
    '<div class="table-note muted">Passwords are write-only: leave the field blank while editing to keep the current password.</div>' +
    '</div></div>';

  view.insertAdjacentHTML('beforeend',
    '<div class="modal-backdrop" id="userModal"><div class="modal wide">' +
    '<div class="modal-head"><h3 id="umTitle">Add User</h3><button class="x" data-close>✕</button></div>' +
    '<div class="modal-body"><form id="userForm"><div class="form-grid">' +
    '<div class="field"><label>Username <span class="req">*</span></label><input name="username" required></div>' +
    '<div class="field"><label>Email <span class="req">*</span></label><input name="email" type="email" required></div>' +
    '<div class="field"><label>Password <span class="req" id="pwReq">*</span></label><input name="password" type="password" id="pwInput" autocomplete="new-password">' +
    '<div class="help" id="pwHelp">Required for new users. Leave blank when editing to keep the current password.</div></div>' +
    '<div class="field"><label>Role <span class="req">*</span></label><select name="role" required><option>ADMIN</option><option>FACULTY</option><option>STUDENT</option></select></div>' +
    '<div class="field full" id="linkField"><label>Linked Student Record (STUDENT role only)</label>' +
    '<select name="studentId" id="studentSelect"><option value="">— none —</option></select>' +
    '<div class="help">Students can only see their own academic data after this link is set.</div></div>' +
    '<div class="field full"><label class="checkbox-row" style="padding:0"><input type="checkbox" name="enabled" id="enabledChk" checked> Account enabled</label></div>' +
    '</div><div class="form-error"></div>' +
    '<div class="form-foot"><button type="button" class="btn ghost" data-close>Cancel</button>' +
    '<button type="submit" class="btn primary" id="saveBtn">Save User</button></div>' +
    '</form></div></div></div>');

  initModals();

  async function loadLookups() {
    students = await api('/api/students');
    fillSelect($('#studentSelect'), students, 'studentId',
      s => s.rollNumber + ' — ' + fullName(s), '— none —');
  }

  async function load() {
    const tbody = $('#rows');
    renderLoading(tbody, 7);
    try {
      allRows = await api('/api/users');
      const q = ($('#q').value || '').trim().toLowerCase();
      const role = $('#fRole').value;
      if (q) allRows = allRows.filter(u => (u.username || '').toLowerCase().includes(q) || (u.email || '').toLowerCase().includes(q));
      if (role) allRows = allRows.filter(u => u.role === role);

      $('#countLbl').textContent = allRows.length + ' user(s)';
      if (!allRows.length) renderEmpty(tbody, 7, 'No users found', 'Adjust filters or create a user.');
      else tbody.innerHTML = allRows.map(u =>
        '<tr data-id="' + u.id + '">' +
        '<td>' + u.id + '</td>' +
        '<td><b>' + esc(u.username) + '</b></td>' +
        '<td class="muted">' + esc(u.email) + '</td>' +
        '<td>' + statusBadge(u.role) + '</td>' +
        '<td class="muted">' + esc(u.studentId != null ? (students.find(s => s.studentId === u.studentId) ?
          (students.find(s => s.studentId === u.studentId).rollNumber + ' — ' + fullName(students.find(s => s.studentId === u.studentId))) : 'Student #' + u.studentId) : '—') + '</td>' +
        '<td>' + (u.enabled ? '<span class="badge green">Enabled</span>' : '<span class="badge red">Disabled</span>') + '</td>' +
        '<td class="actions">' +
        '<button class="btn ghost sm" data-act="toggle">' + (u.enabled ? 'Disable' : 'Enable') + '</button>' +
        '<button class="btn ghost sm" data-act="edit">Edit</button>' +
        '<button class="btn danger sm" data-act="del">Delete</button>' +
        '</td></tr>').join('');
    } catch (err) { renderError(tbody, 7, err, load); }
  }

  $('#q').addEventListener('input', load);
  $('#fRole').addEventListener('change', load);

  function openForm(u) {
    const form = $('#userForm');
    form.reset(); clearFormErrors(form);
    $('#umTitle').textContent = u ? 'Edit User — ' + u.username : 'Add User';
    form.dataset.id = u ? u.id : '';
    form.username.value = u ? u.username : '';
    form.email.value = u ? u.email : '';
    form.password.value = '';
    form.password.required = !u;      // password mandatory only for new users
    form.role.value = u ? u.role : 'STUDENT';
    $('#enabledChk').checked = u ? !!u.enabled : true;
    form.studentId.value = u && u.studentId != null ? u.studentId : '';
    form.studentId.disabled = form.role.value !== 'STUDENT';
    openModal('userModal');
  }

  $('#addBtn').onclick = () => openForm(null);

  // only STUDENT accounts can carry a student-record link
  $('#userForm [name=role]').addEventListener('change', e => {
    $('#studentSelect').disabled = e.target.value !== 'STUDENT';
  });

  $('#userForm').addEventListener('submit', async e => {
    e.preventDefault();
    const form = e.target;
    const json = formToJSON(form); // blank password is skipped -> keep current
    json.enabled = $('#enabledChk').checked;
    // always explicit: 0 = unlink (backend semantics), so choosing "— none —" clears the link
    json.studentId = Number($('#studentSelect').value) || 0;
    if (json.role !== 'STUDENT') json.studentId = 0;
    const id = form.dataset.id;
    try {
      if (id) { await api('/api/users/' + id, { method: 'PUT', body: JSON.stringify(json) }); toast('User updated successfully.'); }
      else { await api('/api/users', { method: 'POST', body: JSON.stringify(json) }); toast('User created successfully.'); }
      closeModal('userModal');
      load();
    } catch (err) { showFormErrors(form, err.message); }
  });

  $('#rows').addEventListener('click', async e => {
    const btn = e.target.closest('button[data-act]');
    if (!btn) return;
    const id = btn.closest('tr').dataset.id;
    const u = allRows.find(x => String(x.id) === id);

    if (btn.dataset.act === 'toggle') {
      try {
        const payload = {
          username: u.username, email: u.email, role: u.role,
          enabled: !u.enabled,
          studentId: u.studentId != null ? u.studentId : null
        };
        await api('/api/users/' + id, { method: 'PUT', body: JSON.stringify(payload) });
        toast('User ' + (u.enabled ? 'disabled' : 'enabled') + '.');
        load();
      } catch (err) { toast(err.message, 'error'); }
    } else if (btn.dataset.act === 'edit') {
      openForm(u);
    } else {
      const ok = await confirmDialog('Delete User?', 'Are you sure you want to delete the account "' + u.username + '"? This cannot be undone.');
      if (!ok) return;
      try { await api('/api/users/' + id, { method: 'DELETE' }); toast('User deleted successfully.'); load(); }
      catch (err) { toast(err.message, 'error'); }
    }
  });

  await loadLookups();
  await load();
};
