const root = document.documentElement;
const themeButton = document.getElementById('themeButton');
const themeLabel = themeButton?.querySelector('.theme-label');
const savedTheme = localStorage.getItem('theme');
let downloadMetadata = null;
if (savedTheme) root.dataset.theme = savedTheme;

function syncTheme() {
  const dark = root.dataset.theme === 'dark';
  if (themeLabel) themeLabel.textContent = dark ? 'Dark' : 'Light';
  document.querySelector('meta[name="theme-color"]').setAttribute('content', dark ? '#08111f' : '#f8fafc');
}
if (themeButton) themeButton.addEventListener('click', () => {
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
  if (!group) return;
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
  if (!errorMessage) return;
  errorMessage.style = "display: block;";
  errorMessage.textContent = 'An error occured, please download manually below.';
  const button = document.getElementById('downloadButton');
  if (!button) return;
  button.href = 'https://github.com/UnKabaraQuiDev/modelizer-next/releases';
  button.textContent = 'Open releases';
}

function updateDownloadFromMetadata() {
  if (!document.getElementById('osChoices') || !document.getElementById('buildChoices')) return;
  const osKey = selectedKey('osChoices');
  const buildKey = selectedKey('buildChoices');
  const asset = findAsset(osKey, buildKey);
  if (!asset) {
    console.log(`Asset not found for: ${osKey} ${buildKey}`)
    setDownloadUnavailable();
    return;
  }
  const button = document.getElementById('downloadButton');
  if (!button) return;
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
  const osChoices = document.getElementById('osChoices');
  if (!osChoices) return;
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
      if (!document.getElementById('buildChoices')) return;
      document.querySelectorAll("#buildChoices>.choice").forEach(b => {
          b.setAttribute("aria-pressed", "false");
      });

      document.querySelector("#buildChoices>.choice[data-key='updater']").setAttribute("aria-pressed", "true");
}

selectSystemBuild();
selectBuildType();
setupChoices('osChoices');
setupChoices('buildChoices');

const yearElement = document.getElementById('year');
if (yearElement) yearElement.textContent = new Date().getFullYear();
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

    const channels = order.map(key => {
      const version = versions[key];
      const info = channelInfo[key];

      return {
        key,
        ...info,
        version: version.version,
        releaseUrl: version.releaseUrl,
        buildDate: version.updatedAt
      };
    });

    renderChannelRows(channels);
  } catch (error) {
    renderChannelError();
  }
}

loadUpdateChannels();

const menuButton = document.getElementById('menuButton');
const navElement = menuButton?.closest('nav');
if (menuButton && navElement) {
  menuButton.addEventListener('click', () => {
    const open = navElement.classList.toggle('menu-open');
    menuButton.setAttribute('aria-expanded', open ? 'true' : 'false');
  });

  navElement.querySelectorAll('.links a').forEach(link => {
    link.addEventListener('click', () => {
      navElement.classList.remove('menu-open');
      menuButton.setAttribute('aria-expanded', 'false');
    });
  });
}


function markCurrentTimelinePage() {
  const currentPath = window.location.pathname === '/' ? '/index.html' : window.location.pathname;
  document.querySelectorAll('.side-timeline a').forEach(link => {
    const path = new URL(link.getAttribute('href'), window.location.origin).pathname;
    if (path === currentPath) {
      link.setAttribute('aria-current', 'page');
    }
  });
}

const sponsorPromptKey = 'modelizer-next-sponsor-prompt-dismissed';
let pendingDownloadUrl = '';

function sponsorPromptDismissed() {
  return localStorage.getItem(sponsorPromptKey) === 'true';
}

function ensureSponsorPrompt() {
  let modal = document.getElementById('sponsorPrompt');
  if (modal) return modal;

  modal = document.createElement('div');
  modal.className = 'sponsor-modal';
  modal.id = 'sponsorPrompt';
  modal.hidden = true;
  modal.setAttribute('role', 'dialog');
  modal.setAttribute('aria-modal', 'true');
  modal.setAttribute('aria-labelledby', 'sponsorPromptTitle');
  modal.innerHTML = `
    <div class="sponsor-modal-backdrop" data-sponsor-close></div>
    <div class="sponsor-modal-card">
      <h2 id="sponsorPromptTitle">Support the project?</h2>
      <p>Modelizer Next is free and open source. You can support the work through GitHub Sponsors.</p>
      <div class="sponsor-modal-actions">
        <a class="btn primary" href="https://github.com/sponsors/UnKabaraQuiDev" target="_blank" rel="noopener">Sponsor the project</a>
        <button class="btn" type="button" data-sponsor-close>Not now</button>
      </div>
      <button class="sponsor-modal-muted" type="button" id="dismissSponsorPrompt">Do not show again</button>
    </div>
  `;
  document.body.appendChild(modal);

  modal.querySelectorAll('[data-sponsor-close]').forEach(element => {
    element.addEventListener('click', () => closeSponsorPrompt(false));
  });

  modal.querySelector('#dismissSponsorPrompt').addEventListener('click', () => closeSponsorPrompt(true));

  document.addEventListener('keydown', event => {
    if (event.key === 'Escape' && !modal.hidden) closeSponsorPrompt(false);
  });

  return modal;
}

function showSponsorPrompt(downloadUrl) {
  pendingDownloadUrl = downloadUrl || pendingDownloadUrl;
  const modal = ensureSponsorPrompt();
  modal.hidden = false;
}

function closeSponsorPrompt(rememberChoice) {
  const modal = document.getElementById('sponsorPrompt');
  if (rememberChoice) {
    localStorage.setItem(sponsorPromptKey, 'true');
  }
  if (modal) modal.hidden = true;
}

function setupDownloadSponsorPrompt() {
  document.querySelectorAll('#downloadButton').forEach(button => {
    button.addEventListener('click', event => {
      if (sponsorPromptDismissed()) return;

      event.preventDefault();
      const downloadUrl = button.href;
      const opened = window.open(downloadUrl, '_blank', 'noopener');
      showSponsorPrompt(downloadUrl);

      if (!opened) {
        const firstAction = document.querySelector('#sponsorPrompt .sponsor-modal-actions a, #sponsorPrompt .sponsor-modal-actions button');
        if (firstAction) firstAction.focus();
      }
    });
  });
}

markCurrentTimelinePage();
setupDownloadSponsorPrompt();
