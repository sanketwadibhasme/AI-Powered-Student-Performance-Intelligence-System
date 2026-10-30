/* My Academics — student self-service: marks, attendance, enrolled subjects */
window.view_myacademics = async function (view) {
  view.innerHTML = '<div class="state"><div class="spin"></div>Loading…</div>';

  const [marks, attendanceList, enrollments] = await Promise.all([
    api('/api/students/me/marks'),
    api('/api/students/me/attendance'),
    api('/api/students/me/enrollments').catch(() => [])
  ]);

  // Enrollment JSON omits department/faculty; join faculty names from the subjects list.
  let subjects = [];
  try { subjects = await api('/api/subjects'); } catch (e) { /* best effort */ }
  const facultyOf = sid => {
    const s = subjects.find(x => x.subjectId === sid);
    return s && s.faculty ? s.faculty.facultyName : null;
  };

  const marksBySubj = {};
  marks.forEach(m => {
    const s = m.subject ? m.subject.subjectName : '—';
    if (!marksBySubj[s]) marksBySubj[s] = { sum: 0, n: 0 };
    marksBySubj[s].sum += m.marksObtained; marksBySubj[s].n++;
  });

  view.innerHTML =
    '<div class="page-head"><div><h2>My Academics</h2><div class="desc">Your marks, attendance and enrolled subjects.</div></div></div>' +

    '<div class="tabs">' +
    '  <button data-tab="marks" class="active">Marks (' + marks.length + ')</button>' +
    '  <button data-tab="att">Attendance (' + attendanceList.length + ')</button>' +
    '  <button data-tab="subj">Subjects (' + enrollments.length + ')</button>' +
    '</div>' +

    '<div id="tab-marks" class="card"><div class="card-body">' +
    '  <div class="table-wrap"><table class="data"><thead><tr><th>Subject</th><th>Exam</th><th>Date</th><th>Score</th></tr></thead><tbody>' +
    (marks.length ? marks.map(m => '<tr><td>' + esc(m.subject ? m.subject.subjectName : '—') + '</td><td>' + esc(m.exam ? m.exam.examName : '—') + '</td><td>' + fmtDate(m.exam ? m.exam.examDate : '') + '</td><td>' + marksBadge(m.marksObtained) + '</td></tr>').join('')
      : '<tr><td colspan="4"><div class="state"><div class="big">📭</div><div class="msg">No marks recorded yet</div></div></td></tr>') +
    '  </tbody></table></div>' +
    '  <h3 class="card-title mt">Subject averages</h3><div id="subjChart"></div>' +
    '</div></div>' +

    '<div id="tab-att" class="card" style="display:none"><div class="card-body">' +
    '  <div class="table-wrap" style="max-height:460px;overflow-y:auto"><table class="data"><thead><tr><th>Date</th><th>Subject</th><th>Status</th></tr></thead><tbody>' +
    (attendanceList.length ? attendanceList.slice().reverse().map(a => '<tr><td>' + fmtDate(a.attendanceDate) + '</td><td>' + esc(a.subject ? a.subject.subjectName : '—') + '</td><td>' + statusBadge(a.status) + '</td></tr>').join('')
      : '<tr><td colspan="3"><div class="state"><div class="big">📭</div><div class="msg">No attendance recorded yet</div></div></td></tr>') +
    '  </tbody></table></div>' +
    '</div></div>' +

    '<div id="tab-subj" class="card" style="display:none"><div class="card-body">' +
    (enrollments.length
      ? '<div class="table-wrap"><table class="data"><thead><tr><th>Subject</th><th>Code</th><th>Credits</th><th>Faculty</th><th>Semester</th><th>Academic Year</th></tr></thead><tbody>' +
        enrollments.map(en => '<tr><td>' + esc(en.subject ? en.subject.subjectName : '—') + '</td><td><span class="badge violet">' + esc(en.subject ? en.subject.subjectCode : '—') + '</span></td>' +
          '<td>' + (en.subject ? en.subject.credits : '—') + '</td><td>' + esc(facultyOf(en.subject ? en.subject.subjectId : null) || '—') + '</td>' +
          '<td>' + en.semester + '</td><td>' + esc(en.academicYear) + '</td></tr>').join('') +
        '</tbody></table></div>'
      : '<div class="state"><div class="big">📭</div><div class="msg">No enrollments found</div><div class="sub">Contact your department office.</div></div>') +
    '</div></div>';

  $all('.tabs button').forEach(b => b.addEventListener('click', () => {
    $all('.tabs button').forEach(x => x.classList.remove('active'));
    b.classList.add('active');
    ['marks', 'att', 'subj'].forEach(t => $('#tab-' + t).style.display = t === b.dataset.tab ? '' : 'none');
  }));

  renderBarChart($('#subjChart'),
    Object.keys(marksBySubj).map(s => ({ label: s, value: marksBySubj[s].n ? marksBySubj[s].sum / marksBySubj[s].n : 0, cls: 'green' }))
      .filter(i => i.value > 0), { max: 100 });
};
