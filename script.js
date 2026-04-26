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