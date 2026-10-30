/* Student detail — the central academic profile: identity + academics + intelligence + actions */
window.view_student = async function (view) {
  const id = sessionStorage.getItem('spis_selectedStudent');
  if (!id) { view.innerHTML = '<div class="card"><div class="card-body"><div class="state"><div class="big">👈</div><div class="msg">No student selected</div><div class="sub">Open Students and click View.</div></div></div></div>'; return; }

  view.innerHTML = '<div class="state"><div class="spin"></div>Loading student…</div>';

  let st;
  try { st = await api('/api/students/' + id); }
  catch (err) {
    view.innerHTML = '<div class="card"><div class="card-body"><div class="state error"><div class="big">⚠️</div><div class="msg">' + esc(err.message) + '</div>' +
      '<button class="btn ghost sm" onclick="location.hash=\'#students\'">Back to Students</button></div></div></div>';
    return;
  }

  const [avg, att, marks, attendance, enrollments, ranking] = await Promise.all([
    api('/api/analytics/student/' + id + '/student-average-marks').catch(() => null),
    api('/api/analytics/student/' + id + '/student-attendance-percentage').catch(() => null),
    api('/api/marks/search' + qs({ studentId: id })).catch(() => []),
    api('/api/attendance/search' + qs({ studentId: id })).catch(() => []),
    api('/api/enrollments/search' + qs({ studentId: id })).catch(() => []),
    api('/api/reports/student-ranking').catch(() => [])
  ]);
  const rank = ranking.find(r => r.studentId === Number(id));

  /* per-subject attendance for enrolled subjects */
  const subjAtt = {};
  await Promise.all(enrollments.map(async en => {
    if (!en.subject) return;
    try {
      const pctv = await api('/api/analytics/subject/' + en.subject.subjectId + '/subject-attendance-percentage');
      subjAtt[en.subject.subjectId] = pctv; // subject-wide (not per-student); shown as context
    } catch (e) { /* ignore */ }
  }));

  view.innerHTML =
    '<div class="page-head"><div class="flex">' +
    '  <div class="avatar" style="width:52px;height:52px;font-size:19px;border-radius:14px;background:linear-gradient(135deg,#2563eb,#7c3aed);color:#fff;display:flex;align-items:center;justify-content:center;font-weight:700">' + initials(fullName(st)) + '</div>' +
    '  <div><h2>' + esc(fullName(st)) + '</h2><div class="desc">' + esc(st.rollNumber) + ' · ' + esc(st.department ? st.department.departmentName : '') + ' · Semester ' + st.currentSemester + ' · ' + statusBadge(st.status) + '</div></div>' +
    '</div>' +
    '<div class="page-actions"><button class="btn ghost" onclick="location.hash=\'#students\'">← Back</button>' +
    '<a class="btn ghost" href="#marks" onclick="sessionStorage.setItem(\'spis_subject\',\'\')">✏️ Add Marks</a>' +
    '<a class="btn ghost" href="#attendance">✋ Record Attendance</a>' +
    '<button class="btn violet" id="aiBtn">🤖 Run AI Analysis</button></div></div>' +

    '<div class="kpis">' +
    '  <div class="kpi blue"><div class="label">Average Marks</div><div class="value">' + (avg != null ? num(avg) : '—') + '</div></div>' +
    '  <div class="kpi green"><div class="label">Attendance</div><div class="value">' + (att != null ? pct(att) : '—') + '</div></div>' +
    '  <div class="kpi violet"><div class="label">Subjects Enrolled</div><div class="value">' + enrollments.length + '</div></div>' +
    '  <div class="kpi amber"><div class="label">Class Rank</div><div class="value">' + (rank ? '#' + rank.rank : '—') + '</div></div>' +
    '</div>' +

    '<div class="grid-2-1 mb">' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">Performance trend (marks by exam date)</h3>' +
    (marks.length ? '<div id="trend"></div>' : '<div class="state"><div class="big">📊</div><div class="msg">No marks recorded yet</div><div class="sub">The trend appears after the first assessment.</div></div>') +
    '  </div></div>' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">Basic information</h3>' +
    '    <div class="list-tile"><div class="ico">✉️</div><div class="grow"><b>' + esc(st.email) + '</b><span>Email</span></div></div>' +
    '    <div class="list-tile"><div class="ico">📞</div><div class="grow"><b>' + esc(st.phone) + '</b><span>Phone</span></div></div>' +
    '    <div class="list-tile"><div class="ico">🚻</div><div class="grow"><b>' + esc(st.gender) + '</b><span>Gender</span></div></div>' +
    '    <div class="list-tile"><div class="ico">🎂</div><div class="grow"><b>' + fmtDate(st.dob) + '</b><span>Date of birth</span></div></div>' +
    '    <div class="list-tile"><div class="ico">📅</div><div class="grow"><b>' + esc(st.admissionYear) + '</b><span>Admission year</span></div></div>' +
    '  </div></div>' +
    '</div>' +

    '<div class="grid-2 mb">' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">Marks by subject</h3>' +
    '    <div class="table-wrap"><table class="data"><thead><tr><th>Subject</th><th>Exam</th><th>Date</th><th>Score</th></tr></thead><tbody>' +
    (marks.length ? marks.map(m => '<tr><td>' + esc(m.subject ? m.subject.subjectName : '—') + '</td><td>' + esc(m.exam ? m.exam.examName : '—') + '</td><td class="muted">' + fmtDate(m.exam ? m.exam.examDate : null) + '</td><td>' + marksBadge(m.marksObtained) + '</td></tr>').join('')
      : '<tr><td colspan="4" class="muted">No marks recorded.</td></tr>') +
    '    </tbody></table></div>' +
    '  </div></div>' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">Enrolled subjects</h3>' +
    (enrollments.length ? enrollments.map(en =>
      '<div class="list-tile"><div class="ico">📚</div>' +
      '<div class="grow"><b>' + esc(en.subject ? en.subject.subjectName : '—') + '</b><span>' + esc(en.subject ? en.subject.subjectCode : '') + ' · Sem ' + en.semester + ' · ' + esc(en.academicYear) + '</span></div></div>').join('')
      : '<div class="state"><div class="big">📭</div><div class="msg">No enrollments</div><div class="sub">Enroll this student in subjects from the Enrollments module.</div></div>') +
    '    <div class="mt muted">Attendance records: ' + attendance.length + ' (' + (attendance.filter(a => a.status === 'PRESENT').length) + ' present)</div>' +
    '  </div></div>' +
    '</div>' +

    '<div id="aiSection"></div>';

  /* trend chart: average marks per exam, ordered by date */
  if (marks.length) {
    const byExam = {};
    marks.forEach(m => {
      if (!m.exam) return;
      const key = m.exam.examId;
      if (!byExam[key]) byExam[key] = { name: m.exam.examName, date: m.exam.examDate, sum: 0, n: 0 };
      byExam[key].sum += m.marksObtained; byExam[key].n++;
    });
    const trendItems = Object.values(byExam)
      .sort((a, b) => String(a.date).localeCompare(String(b.date)))
      .map(e => ({ label: e.name, value: e.n ? e.sum / e.n : 0, cls: 'green' }));
    renderBarChart($('#trend'), trendItems, { max: 100, decimals: 1 });
  }

  /* AI analysis (runs on real data; result includes data quality) */
  $('#aiBtn').onclick = async () => {
    $('#aiSection').innerHTML = '<div class="card"><div class="card-body"><div class="state"><div class="spin"></div>Running model on real academic data…</div></div></div>';
    const result = await runAI($('#aiBtn'), '/api/ai/students/' + id + '/insights', {},
      { target: $('#aiSection'), retry: () => $('#aiBtn').click() });
    if (result) {
      $('#aiSection').innerHTML =
        '<div class="page-head"><div><h2 style="font-size:16px">AI Academic Analysis</h2>' +
        '<div class="desc">Model estimates on this student\u2019s real academic data; recommendations are generated guidance.</div></div></div>' +
        aiResultHTML(result, { showRecommendations: true }) +
        '<div class="flex mt"><a class="btn ghost sm" href="#ai">Open AI Intelligence for what-if simulation →</a></div>';
    }
  };
};
