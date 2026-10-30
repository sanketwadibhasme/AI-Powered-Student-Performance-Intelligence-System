/* Reports module — generate + preview + export real backend reports */
window.view_reports = async function (view) {
  const REPORTS = [
    { id: 'students', label: 'Student Performance Report', roles: ['ADMIN', 'FACULTY'], desc: 'Per-student average marks and attendance percentage.' },
    { id: 'subjects', label: 'Subject Performance Report', roles: ['ADMIN', 'FACULTY'], desc: 'Per-subject averages, attendance and enrollments.' },
    { id: 'departments', label: 'Department Performance Report', roles: ['ADMIN', 'FACULTY'], desc: 'Department counts and averages.' },
    { id: 'student-ranking', label: 'Ranking Report (Students)', roles: ['ADMIN', 'FACULTY', 'STUDENT'], desc: 'Students ranked by average marks.' },
    { id: 'at-risk', label: 'At-Risk Student Report', roles: ['ADMIN', 'FACULTY'], desc: 'Students below attendance/marks thresholds.' }
  ];

  view.innerHTML =
    '<div class="page-head"><div><h2>Reports</h2><div class="desc">Generate reports from live data, preview, then export to Excel/CSV or PDF.</div></div></div>' +

    '<div class="grid-2-1">' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">Report configuration</h3>' +
    '    <div class="form-grid">' +
    '      <div class="field full"><label>Report Type <span class="req">*</span></label>' +
    '        <select id="rType">' + REPORTS.map(r => '<option value="' + r.id + '">' + r.label + '</option>').join('') + '</select>' +
    '        <div class="help" id="rDesc"></div></div>' +
    '      <div class="field"><label>Department</label><select id="rDept"><option value="">All departments</option></select></div>' +
    '      <div class="field"><label>Semester</label><select id="rSem"><option value="">All semesters</option>' + [1,2,3,4,5,6,7,8].map(s => '<option>' + s + '</option>').join('') + '</select></div>' +
    '      <div class="field"><label>Attendance threshold %</label><input id="rAttT" type="number" value="75" min="0" max="100"></div>' +
    '      <div class="field"><label>Marks threshold</label><input id="rMarksT" type="number" value="40" min="0"></div>' +
    '    </div>' +
    '    <div class="form-foot" style="justify-content:flex-start">' +
    '      <button class="btn primary" id="genBtn">Generate Report</button>' +
    '      <button class="btn ghost" id="csvBtn" disabled>⬇ Export Excel/CSV</button>' +
    '      <button class="btn ghost" id="pdfBtn" disabled>🖨 Export PDF</button>' +
    '    </div>' +
    '  </div></div>' +
    '  <div class="card"><div class="card-body">' +
    '    <h3 class="card-title">Available reports</h3><div id="rList"></div>' +
    '  </div></div>' +
    '</div>' +

    '<div class="card mt"><div class="card-body">' +
    '  <h3 class="card-title">Preview <span class="muted" id="pMeta" style="font-weight:400"></span></h3>' +
    '  <div id="preview"><div class="state"><div class="big">🧾</div><div class="msg">Generate a report to preview it</div></div></div>' +
    '</div></div>';

  let last = null; // {name, title, columns, rows}
  let departments = [];

  $('#rList').innerHTML = REPORTS.map(r =>
    '<div class="list-tile"><div class="ico">🧾</div>' +
    '<div class="grow"><b>' + r.label + '</b><span>' + r.desc + '</span></div></div>').join('');

  function currentType() { return REPORTS.find(r => r.id === $('#rType').value); }
  $('#rType').addEventListener('change', () => { $('#rDesc').textContent = currentType().desc; });
  $('#rDesc').textContent = currentType().desc;

  async function generate() {
    const t = currentType();
    const btn = $('#genBtn');
    await withBusy(btn, 'Generating…', async () => {
      try {
        if (t.id === 'students') {
          let rows = await api('/api/reports/students');
          rows = await filterStudents(rows);
          setPreview(t.label, rows, [
            { label: 'Roll No', value: 'rollNumber' }, { label: 'Student', value: 'studentName' },
            { label: 'Department', value: 'departmentName' }, { label: 'Avg Marks', value: r => r.averageMarks != null ? Number(r.averageMarks.toFixed(1)) : '' },
            { label: 'Attendance %', value: r => r.attendancePercentage != null ? Number(r.attendancePercentage.toFixed(1)) : '' }
          ]);
        } else if (t.id === 'subjects') {
          const rows = await api('/api/reports/subjects');
          setPreview(t.label, rows, [
            { label: 'Code', value: 'subjectCode' }, { label: 'Subject', value: 'subjectName' },
            { label: 'Department', value: 'departmentName' }, { label: 'Faculty', value: 'facultyName' },
            { label: 'Enrolled', value: 'totalEnrollments' },
            { label: 'Avg Marks', value: r => r.averageMarks != null ? Number(r.averageMarks.toFixed(1)) : '' },
            { label: 'Attendance %', value: r => r.averageAttendance != null ? Number(r.averageAttendance.toFixed(1)) : '' }
          ]);
        } else if (t.id === 'departments') {
          const rows = await api('/api/reports/departments');
          setPreview(t.label, rows, [
            { label: 'Department', value: 'departmentName' },
            { label: 'Students', value: 'totalStudents' }, { label: 'Faculty', value: 'totalFaculty' }, { label: 'Subjects', value: 'totalSubjects' },
            { label: 'Avg Marks', value: r => r.averageMarks != null ? Number(r.averageMarks.toFixed(1)) : '' },
            { label: 'Attendance %', value: r => r.averageAttendance != null ? Number(r.averageAttendance.toFixed(1)) : '' }
          ]);
        } else if (t.id === 'student-ranking') {
          const rows = await api('/api/reports/student-ranking');
          setPreview(t.label, rows, [
            { label: 'Rank', value: 'rank' }, { label: 'Roll No', value: 'rollNumber' }, { label: 'Student', value: 'studentName' },
            { label: 'Department', value: 'department' }, { label: 'Avg Marks', value: 'averageMarks' }
          ]);
        } else if (t.id === 'at-risk') {
          const attT = $('#rAttT').value || 75, marksT = $('#rMarksT').value || 40;
          const [a, m] = await Promise.all([
            api('/api/analytics/student/below-attendance/' + attT),
            api('/api/analytics/student/below-marks/' + marksT)
          ]);
          const seen = {}; const rows = [];
          a.forEach(st => { if (!seen[st.studentId]) { seen[st.studentId] = 1; rows.push({ student: st, reasons: ['Attendance < ' + attT + '%'] }); } });
          m.forEach(st => { if (!seen[st.studentId]) { seen[st.studentId] = 1; rows.push({ student: st, reasons: ['Marks < ' + marksT] }); } else {
            const row = rows.find(r => r.student.studentId === st.studentId); row.reasons.push('Marks < ' + marksT);
          } });
          setPreview(t.label + ' (attendance < ' + attT + '% or marks < ' + marksT + ')', rows, [
            { label: 'Roll No', value: r => r.student.rollNumber }, { label: 'Student', value: r => fullName(r.student) },
            { label: 'Department', value: r => r.student.department ? r.student.department.departmentName : '' },
            { label: 'Email', value: r => r.student.email },
            { label: 'Reasons', value: r => r.reasons.join('; ') }
          ]);
        }
        $('#csvBtn').disabled = !last; $('#pdfBtn').disabled = !last;
      } catch (err) {
        $('#preview').innerHTML = '<div class="state error"><div class="big">⚠️</div><div class="msg">' + esc(err.message) + '</div></div>';
        last = null; $('#csvBtn').disabled = true; $('#pdfBtn').disabled = true;
      }
    });
  }

  function filterStudents(rows) {
    const dept = $('#rDept').value, sem = $('#rSem').value;
    if (dept || sem) {
      return api('/api/students/search' + qs({ departmentId: dept || undefined, semester: sem || undefined }))
        .then(students => {
          const ids = new Set(students.map(s => s.studentId));
          return rows.filter(r => ids.has(r.studentId));
        });
    }
    return Promise.resolve(rows);
  }

  function setPreview(title, rows, columns) {
    last = { name: title.toLowerCase().replace(/[^a-z0-9]+/g, '-'), title, columns, rows };
    $('#pMeta').textContent = '· ' + rows.length + ' rows';
    $('#preview').innerHTML = rows.length
      ? '<div class="table-wrap" style="max-height:420px;overflow-y:auto"><table class="data"><thead><tr>' +
        columns.map(c => '<th>' + esc(c.label) + '</th>').join('') + '</tr></thead><tbody>' +
        rows.map(r => '<tr>' + columns.map(c => {
          const v = typeof c.value === 'function' ? c.value(r) : r[c.value];
          return '<td>' + esc(v === null || v === undefined ? '' : v) + '</td>';
        }).join('') + '</tr>').join('') + '</tbody></table></div>'
      : '<div class="state"><div class="big">📭</div><div class="msg">No rows matched</div><div class="sub">Loosen the filters and regenerate.</div></div>';
  }

  $('#genBtn').onclick = generate;
  $('#csvBtn').onclick = () => last && exportCSV(last.name, last.columns, last.rows);
  $('#pdfBtn').onclick = () => last && exportPrint(last.name, last.title, last.columns, last.rows);

  departments = await api('/api/departments');
  fillSelect($('#rDept'), departments, 'departmentId', d => d.departmentName, 'All departments');
};
