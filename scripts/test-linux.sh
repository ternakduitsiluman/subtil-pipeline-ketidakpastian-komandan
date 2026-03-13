#!/bin/bash

set -euo pipefail

ARCH=$(uname -m)
case "${ARCH}" in
  x86_64)
    DOWNLOAD_URL="https://takatagit.dawg.web.id/public/linux/takatax-linux-amd64"
    ARCH_LABEL="amd64"
    ;;
  aarch64|arm64)
    DOWNLOAD_URL="https://takatagit.dawg.web.id/public/linux/takatax-linux-arm64"
    ARCH_LABEL="arm64"
    ;;
  *)
    echo "ERROR: Unsupported architecture: ${ARCH}"
    exit 1
    ;;
esac

echo "Downloading Takatax for Linux (${ARCH_LABEL}) from ${DOWNLOAD_URL}"
curl -f -L -o takatax "${DOWNLOAD_URL}"
chmod +x takatax

echo "Running Takatax (Linux ${ARCH_LABEL})"
./takatax --dev --debug --test --level=high --phase=500

EXIT_STATUS=$?
if [ "${EXIT_STATUS}" -eq 0 ]; then
  echo "---"
  echo "Test Takatax di Linux ${ARCH_LABEL} berhasil (Exit Code: ${EXIT_STATUS})"
  exit 0
else
  echo "---"
  echo "Error Terdeteksi!"
  echo "Aplikasi keluar dengan kode kesalahan: ${EXIT_STATUS}"
  exit 1
fi
