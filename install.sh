#!/bin/sh
set -e

KEYRING="/etc/apt/keyrings/nexus.kbra.lu.gpg"
SOURCE_LIST="/etc/apt/sources.list.d/nexus.kbra.lu.list"
KEY_URL="https://raw.githubusercontent.com/UnKabaraQuiDev/modelizer-next/refs/heads/main/nexus-apt.asc"
REPO="https://nexus.kbra.lu/repository/apt-hosted/"

echo "==> Setting up Modelizer Next APT repository"

sudo mkdir -p /etc/apt/keyrings

if [ ! -f "$KEYRING" ]; then
    echo "==> Installing signing key..."
    curl -fsSL "$KEY_URL" \
        | sudo gpg --dearmor -o "$KEYRING"
else
    echo "==> Signing key already installed"
fi

echo "==> Configuring repository..."
echo "deb [signed-by=$KEYRING] $REPO stable main" \
    | sudo tee "$SOURCE_LIST" > /dev/null

echo "==> Updating package lists..."
sudo apt update

# Use the first argument if provided.
choice="${1:-}"

if [ -z "$choice" ]; then
    echo ""
    echo "Select installation type:"
    echo ""
    echo "  1) Standalone (Recommended)"
    echo "     Install the latest release only"
    echo ""
    echo "  2) Updater"
    echo "     Install Modelizer Next with branch switching support"
    echo ""

    while true; do
        printf "Enter your choice [1-2]: "
        read choice

        case "$choice" in
            1|2)
                break
                ;;
            *)
                echo "Invalid choice. Please enter 1 or 2."
                ;;
        esac
    done
else
    case "$choice" in
        1|2)
            ;;
        *)
            echo "Usage: $0 [1|2]"
            echo ""
            echo "  1  Standalone"
            echo "  2  Updater"
            exit 1
            ;;
    esac
fi

case "$choice" in
    1)
        PACKAGE="modelizer-next-app"
        echo "==> Installing standalone Modelizer Next..."
        ;;
    2)
        PACKAGE="modelizer-next"
        echo "==> Installing Modelizer Next with updater support..."
        ;;
esac

sudo apt install -y "$PACKAGE"

echo "==> Done!"
