/* Profile — account identity (+ student profile when linked) */
window.view_profile = async function (view) {
  view.innerHTML = '<div class="state"><div class="spin"></div>Loading…</div>';

  const me = await api('/api/auth/me');
  let student = null;
  if (getRole() === 'STUDENT') {
    // students can only read their own record via the self-service endpoint
    try { student = await api('/api/students/me/profile'); } catch (e) { /* no linked record */ }
  }

  view.innerHTML =
    '<div class="page-head"><div><h2>Profile</h2><div class="desc">Your account identity in SPIS.</div></div>' +
    '<div class="page-actions"><button class="btn danger" id="logoutBtn2">⏻ Logout</button></div></div>' +

    '<div class="grid-2">' +
    '  <div class="card"><div class="card-body">' +
    '    <div class="flex mb" style="gap:14px">' +
    '      <div class="avatar" style="width:52px;height:52px;font-size:19px;border-radius:14px;background:linear-gradient(135deg,#2563eb,#7c3aed);color:#fff;display:flex;align-items:center;justify-content:center;font-weight:700">' + initials(getUsername()) + '</div>' +
    '      <div><b style="font-size:16px">' + esc(getUsername()) + '</b><div class="muted">' + statusBadge(me.role) + '</div></div>' +
    '    </div>' +
    '    <div class="list-tile"><div class="ico">👤</div><div class="grow"><b>' + esc(me.username) + '</b><span>Username</span></div></div>' +
    '    <div class="list-tile"><div class="ico">🛡️</div><div class="grow"><b>' + esc(me.role) + '</b><span>Role</span></div></div>' +
    '    <div class="list-tile"><div class="ico">🔗</div><div class="grow"><b>' + (me.studentId ? 'Student record #' + me.studentId : '—') + '</b><span>Linked student record</span></div></div>' +
    '    <div class="help mt">Account details (username/email/password) are managed by the administrator under Users. JWT sessions last one hour.</div>' +
    '  </div></div>' +

    (student
      ? '<div class="card"><div class="card-body">' +
        '  <h3 class="card-title">Linked student record</h3>' +
        '  <div class="list-tile"><div class="ico">🎓</div><div class="grow"><b>' + esc(fullName(student)) + '</b><span>' + esc(student.rollNumber) + '</span></div></div>' +
        '  <div class="list-tile"><div class="ico">🏛️</div><div class="grow"><b>' + esc(student.department ? student.department.departmentName : '—') + '</b><span>Department</span></div></div>' +
        '  <div class="list-tile"><div class="ico">📅</div><div class="grow"><b>Semester ' + student.currentSemester + '</b><span>Admission year ' + esc(student.admissionYear) + '</span></div></div>' +
        '  <div class="list-tile"><div class="ico">✉️</div><div class="grow"><b>' + esc(student.email) + '</b><span>Email</span></div></div>' +
        '</div></div>'
      : '<div class="card"><div class="card-body"><div class="state"><div class="big">🧑‍💼</div>' +
        '<div class="msg">Staff account</div><div class="sub">No student record is linked to this account.</div></div></div></div>') +
    '</div>';

  $('#logoutBtn2').onclick = logout;
};
