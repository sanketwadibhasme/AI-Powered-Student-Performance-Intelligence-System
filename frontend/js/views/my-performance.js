/* My Performance — student performance breakdown (own data only) */
window.view_myperformance = async function (view) {
  view.innerHTML = '<div class="state"><div class="spin"></div>Loading…</div>';

  const [me, avg, att, marks, ranking] = await Promise.all([
    api('/api/students/me/profile'),
    api('/api/students/me/average-marks').catch(() => null),
    api('/api/students/me/attendance-percentage').catch(() => null),
    api('/api/students/me/marks'),
    api('/api/reports/student-ranking').catch(() => [])
  ]);
  const myId = Number(sessionStorage.getItem('spis_studentId'));
  const mine = ranking.find(r => r.studentId === myId);
  const totalRanked = ranking.length;

  const bySubj = {};
  marks.forEach(m => {
    const key = m.subject ? m.subject.subjectName : '—';
    const code = m.subject ? m.subject.subjectCode : '';
    if (!bySubj[key]) bySubj[key] = { code, sum: 0, n: 0 };
    bySubj[key].sum += m.marksObtained; bySubj[key].n++;
  });
  const subjectRows = Object.keys(bySubj).map(s => ({ subject: s, code: bySubj[s].code, avg: bySubj[s].n ? bySubj[s].sum / bySubj[s].n : 0 }));

  const byExam = {};
  marks.forEach(m => {
    if (!m.exam) return;
    if (!byExam[m.exam.examId]) byExam[m.exam.examId] = { name: m.exam.examName, date: m.exam.examDate, sum: 0, n: 0 };
    byExam[m.exam.examId].sum += m.marksObtained; byExam[m.exam.examId].n++;
  });
  const trend = Object.values(byExam).sort((a, b) => String(a.date).localeCompare(String(b.date)));

  view.innerHTML =
    '<div class="page-head"><div><h2>My Performance</h2><div class="desc">Historical academic data — actuals, not predictions.</div></div></div>' +

    '<div class="kpis">' +
    '  <div class="kpi blue"><div class="label">Average Marks</div><div class="value">' + (avg != null ? num(avg) : '—') + '</div></div>' +
    '  <div class="kpi green"><div class="label">Attendance</div><div class="value">' + (att != null ? pct(att) : '—') + '</div></div>' +
    '  <div class="kpi violet"><div class="label">Class Rank</div><div class="value">' + (mine ? '#' + mine.rank : '—') + '</div>' +
    '    <div class="hint">' + (totalRanked ? 'of ' + totalRanked + ' ranked students' : '&nbsp;') + '</div></div>' +
    '  <div class="kpi amber"><div class="label">Exams Taken</div><div class="value">' + trend.length + '</div></div>' +
    '</div>' +

    '<div class="grid-2 mb">' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">Score trend by exam</h3><div id="trend"></div>' +
    '  </div></div>' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">Subject strength</h3>' +
    (subjectRows.length
      ? subjectRows.sort((a, b) => b.avg - a.avg).map(r => {
          const cls = r.avg >= 70 ? 'green' : r.avg >= 50 ? 'amber' : 'red';
          return '<div style="margin-bottom:12px"><div class="flex" style="justify-content:space-between"><b style="font-size:13px">' + esc(r.subject) + '</b>' + marksBadge(r.avg) + '</div>' +
            '<div class="progress mt" style="margin-top:6px"><i class="' + cls + '" style="width:' + Math.min(100, r.avg) + '%"></i></div></div>';
        }).join('')
      : '<div class="state"><div class="big">📭</div><div class="msg">No marks yet</div></div>') +
    '  </div></div>' +
    '</div>' +

    '<div class="card"><div class="card-body">' +
    '  <h3 class="card-title">Recent results</h3>' +
    '  <div class="table-wrap"><table class="data"><thead><tr><th>Exam</th><th>Date</th><th>Subject</th><th>Score</th></tr></thead><tbody>' +
    (marks.slice().reverse().slice(0, 12).map(m => '<tr><td>' + esc(m.exam ? m.exam.examName : '—') + '</td><td>' + fmtDate(m.exam ? m.exam.examDate : '') + '</td><td>' + esc(m.subject ? m.subject.subjectName : '—') + '</td><td>' + marksBadge(m.marksObtained) + '</td></tr>').join('') ||
      '<tr><td colspan="4" class="muted">No results yet.</td></tr>') +
    '  </tbody></table></div>' +
    '</div></div>' +

    '<div class="mt muted">Want to see what these numbers mean for your future results? Open <a href="#myai">My AI Insights</a>.</div>';

  renderBarChart($('#trend'), trend.map(e => ({ label: e.name, value: e.n ? e.sum / e.n : 0, cls: 'green' })), { max: 100 });
};
