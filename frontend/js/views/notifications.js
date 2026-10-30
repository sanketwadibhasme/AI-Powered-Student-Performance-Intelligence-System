/* Notifications page — alerts derived from live academic data (no fake delivery) */
window.view_notifications = async function (view) {
  const role = getRole();

  view.innerHTML =
    '<div class="page-head"><div><h2>Notifications</h2>' +
    '<div class="desc">Alerts computed from your live academic data every time you open this page.</div></div></div>' +
    '<div class="card"><div class="card-body">' +
    '  <div class="table-note muted mb">ℹ️ Alerts are generated on demand from thresholds. The V0 backend does not yet send email/SMS — this page is the delivery surface.</div>' +
    '  <div id="notifList"><div class="state"><div class="spin"></div>Checking data…</div></div>' +
    '</div></div>';

  const items = await computeAlerts(role);
  const box = $('#notifList');

  if (!items.length) {
    box.innerHTML = '<div class="state"><div class="big">✅</div><div class="msg">Nothing needs attention</div>' +
      '<div class="sub">No low-attendance or low-marks alerts right now.</div></div>';
    return;
  }

  box.innerHTML = items.map(n =>
    '<div class="list-tile" style="padding:14px 0">' +
    '<div class="ico" style="background:' + (n.cls === 'red' ? '#fef2f2' : '#fffbeb') + ';width:40px;height:40px;font-size:18px">' + n.icon + '</div>' +
    '<div class="grow"><b style="font-size:14px">' + esc(n.title) + '</b><span style="font-size:13px">' + esc(n.detail) + '</span></div>' +
    '<span class="badge ' + n.cls + '">' + n.tag + '</span></div>').join('') +

    (role !== 'STUDENT'
      ? '<div class="flex mt"><a class="btn ghost sm" href="#ai">🤖 Open AI Intelligence for deeper analysis</a>' +
        '<a class="btn ghost sm" href="#attendance">Review attendance</a></div>'
      : '<div class="flex mt"><a class="btn ghost sm" href="#myai">🤖 See my AI insights</a></div>');
};
