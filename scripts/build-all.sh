#!/usr/bin/env bash
set -e

echo "=================================================="
echo "🎬 NOLELAMPHIM - LOCAL BUILD SCRIPT"
echo "=================================================="

# Ensure script is run from project root
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

mkdir -p dist

echo "👉 1/2: Đang build Android Mobile APK..."
./gradlew :androidApp:assembleMobileRelease

MOBILE_APK=$(find androidApp/build/outputs/apk/mobile/release -name "*.apk" | head -n 1)
if [ -f "$MOBILE_APK" ]; then
    cp "$MOBILE_APK" dist/Nolelamphim-Mobile.apk
    echo "✅ Android Mobile APK: dist/Nolelamphim-Mobile.apk"
else
    echo "❌ Không tìm thấy Mobile APK!"
fi

echo ""
echo "👉 2/2: Đang build Android TV APK..."
./gradlew :androidApp:assembleTvRelease

TV_APK=$(find androidApp/build/outputs/apk/tv/release -name "*.apk" | head -n 1)
if [ -f "$TV_APK" ]; then
    cp "$TV_APK" dist/Nolelamphim-TV.apk
    echo "✅ Android TV APK: dist/Nolelamphim-TV.apk"
else
    echo "❌ Không tìm thấy TV APK!"
fi

echo ""
echo "=================================================="
echo "🎉 Hoàn tất! File APK được xuất ra tại thư mục dist/:"
ls -lh dist/*.apk 2>/dev/null || true
echo "=================================================="
