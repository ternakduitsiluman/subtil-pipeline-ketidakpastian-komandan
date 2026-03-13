#!/bin/bash

set -euo pipefail

APK_DIR="app/build/outputs/apk/release"
METADATA_FILE="${APK_DIR}/output-metadata.json"

echo "Checking Windows release outputs in ${APK_DIR}"

if [ ! -d "${APK_DIR}" ]; then
  echo "ERROR: Release APK directory not found"
  exit 1
fi

mapfile -t APK_FILES < <(find "${APK_DIR}" -maxdepth 1 -type f -name "*.apk" | sort)

if [ "${#APK_FILES[@]}" -eq 0 ]; then
  echo "ERROR: No release APK files found"
  exit 1
fi

for apk in "${APK_FILES[@]}"; do
  if [ ! -s "${apk}" ]; then
    echo "ERROR: APK is empty: ${apk}"
    exit 1
  fi

  echo "Validated APK: ${apk}"
done

if [ ! -s "${METADATA_FILE}" ]; then
  echo "ERROR: output-metadata.json not found or empty"
  exit 1
fi

echo "Validated metadata: ${METADATA_FILE}"
echo "Windows release artifact test passed"
