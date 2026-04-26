const root = document.documentElement;
const themeButton = document.getElementById('themeButton');
const themeLabel = themeButton.querySelector('.theme-label');
const savedTheme = localStorage.getItem('theme');
let downloadMetadata = null;
if (savedTheme) root.dataset.theme = savedTheme;

function syncTheme() {
  const dark = root.dataset.theme === 'dark';
  themeLabel.textContent = dark ? 'Dark' : 'Light';
  document.querySelector('meta[name="theme-color"]').setAttribute('content', dark ? '#08111f' : '#f8fafc');
}
themeButton.addEventListener('click', () => {
  root.dataset.theme = root.dataset.theme === 'dark' ? 'light' : 'dark';
  localStorage.setItem('theme', root.dataset.theme);
  syncTheme();
});

function selectedButton(groupId) {
  return document.querySelector(`#${groupId} .choice[aria-pressed="true"]`);
}

function selectedKey(groupId) {
  return selectedButton(groupId)?.dataset.key || "";
}

function setupChoices(groupId, outputId) {
  const group = document.getElementById(groupId);
  const initial = selectedButton(groupId);
  group.addEventListener('click', event => {
    const button = event.target.closest('.choice');
    if (!button) return;
    group.querySelectorAll('.choice').forEach(choice => choice.setAttribute('aria-pressed', 'false'));
    button.setAttribute('aria-pressed', 'true');
    updateDownloadFromMetadata();
  });
}
async function loadDownloadMetadata() {
  try {
    const response = await fetch('https://raw.githubusercontent.com/UnKabaraQuiDev/modelizer-next/refs/heads/pages/metadata.json', {
      cache: 'no-store'
    });
    if (!response.ok) throw new Error(`metadata.json returned ${response.status}`);
    downloadMetadata = await response.json();
  } catch (error) {
    downloadMetadata = null;
  }
  updateDownloadFromMetadata();
} 

function findAsset(osKey, buildKey) {
  if (!downloadMetadata || !downloadMetadata.assets) return null;
  return downloadMetadata.assets?.[osKey]?.[buildKey] || null;
}

function setDownloadUnavailable() {
  const errorMessage = document.getElementById('errorMessage');
  errorMessage.style = "display: block;";
  errorMessage.textContent = 'An error occured, please download manually below.';
  const button = document.getElementById('downloadButton');
  button.href = 'https://github.com/UnKabaraQuiDev/modelizer-next/releases';
  button.textContent = 'Open releases';
}

function updateDownloadFromMetadata() {
  const osKey = selectedKey('osChoices');
  const buildKey = selectedKey('buildChoices');
  const asset = findAsset(osKey, buildKey);
  if (!asset) {
    console.log(`Asset not found for: ${osKey} ${buildKey}`)
    setDownloadUnavailable();
    return;
  }
  const button = document.getElementById('downloadButton');
  button.href = asset.url;
  button.textContent = 'Download selected build';
  document.querySelector("#current-version").textContent = downloadMetadata["releaseTag"];
}

function selectSystemBuild() {
  const ua = navigator.userAgent.toLowerCase();
  let os = "windows";
  if (ua.includes("mac")) {
      os = "macos";
  } else if (ua.includes("linux")) {
      os = "linux";
  } else if (ua.includes("win")) {
      os = "windows";
  }
  const button = document.querySelector(
      `#osChoices>.choice[data-key="${os}"]`
  );
  if (button) {
      document.querySelectorAll("#osChoices>.choice").forEach(b => {
          b.setAttribute("aria-pressed", "false");
      });

      button.setAttribute("aria-pressed", "true");
  }
}

function selectBuildType() {
      document.querySelectorAll("#buildChoices>.choice").forEach(b => {
          b.setAttribute("aria-pressed", "false");
      });

      document.querySelector("#buildChoices>.choice[data-key='updater']").setAttribute("aria-pressed", "true");
}

selectSystemBuild();
selectBuildType();
setupChoices('osChoices');
setupChoices('buildChoices');

document.getElementById('year').textContent = new Date().getFullYear();
syncTheme();
loadDownloadMetadata();


// source section

const versionsUrl = 'https://raw.githubusercontent.com/UnKabaraQuiDev/modelizer-next/refs/heads/registry/registry/versions.json';

const channelInfo = {
  release: {
    name: 'Release',
    label: 'Stable',
    icon: '✓'
  },
  snapshot: {
    name: 'Snapshot',
    label: 'Preview',
    icon: '◐'
  },
  nightly: {
    name: 'Nightly',
    label: 'Latest',
    icon: '✦'
  }
};

function formatBuildDate(value) {
  if (!value) return 'Date unavailable';

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'Date unavailable';

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(date);
}

async function loadReleaseDate(tag) {
  try {
    const response = await fetch(`https://api.github.com/repos/UnKabaraQuiDev/modelizer-next/releases/tags/${tag}`, {
      cache: 'no-store'
    });

    if (!response.ok) return null;

    const data = await response.json();
    return data.published_at || data.created_at || null;
  } catch (error) {
    return null;
  }
}

function renderChannelRows(channels) {
  const container = document.getElementById('updateChannels');
  if (!container) return;

  container.innerHTML = channels.map(channel => `
    <article class="channel-row ${channel.key}">
      <div class="channel-icon" aria-hidden="true">${channel.icon}</div>

      <div class="channel-main">
        <span class="channel-label">${channel.label}</span>
        <strong>${channel.name}</strong>
      </div>

      <div class="channel-detail">
        <span>Version</span>
        <strong>${channel.version}</strong>
      </div>

      <div class="channel-detail">
        <span>Build date</span>
        <strong>${formatBuildDate(channel.buildDate)}</strong>
      </div>

      <a class="btn" href="${channel.releaseUrl}" rel="noopener">View release</a>
    </article>
  `).join('');
}

function renderChannelError() {
  const container = document.getElementById('updateChannels');
  if (!container) return;

  container.innerHTML = `
    <article class="channel-row">
      <div class="channel-main">
        <span class="channel-label">Unavailable</span>
        <strong>Could not load update channels</strong>
      </div>

      <div class="channel-detail">
        <span>Status</span>
        <strong>Please open GitHub releases manually.</strong>
      </div>

      <a class="btn" href="https://github.com/UnKabaraQuiDev/modelizer-next/releases" rel="noopener">View releases</a>
    </article>
  `;
}

async function loadUpdateChannels() {
  try {
    const response = await fetch(versionsUrl, {
      cache: 'no-store'
    });

    if (!response.ok) throw new Error(`versions.json returned ${response.status}`);

    const versions = await response.json();
    const order = ['release', 'snapshot', 'nightly'];

    const channels = await Promise.all(order.map(async key => {
      const version = versions[key];
      const info = channelInfo[key];

      return {
        key,
        ...info,
        version: version.version,
        releaseUrl: version.releaseUrl,
        buildDate: await loadReleaseDate(version.tag)
      };
    }));

    renderChannelRows(channels);
  } catch (error) {
    renderChannelError();
  }
}

loadUpdateChannels();
