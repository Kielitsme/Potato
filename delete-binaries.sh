#!/bin/sh
# Hapus file biner (library native, gambar) yang sudah tidak dipakai.
# Jalankan di root repo SETELAH: git apply lowspec-launcher.patch
set -e
git rm -q -f -- app/src/main/assets/app_runtime/lwjgl/3.3.3/natives/liblwjgl_vma.so
git rm -q -f -- app/src/main/assets/app_runtime/lwjgl/3.3.3/natives/libshaderc.so
git rm -q -f -- app/src/main/assets/app_runtime/lwjgl/3.4.1/natives/liblwjgl_vma.so
git rm -q -f -- app/src/main/assets/app_runtime/lwjgl/3.4.1/natives/libshaderc.so
git rm -q -f -- app/src/main/jniLibs/arm64-v8a/libEGL_mesa.so
git rm -q -f -- app/src/main/jniLibs/arm64-v8a/libOSMesa_2121.so
git rm -q -f -- app/src/main/jniLibs/arm64-v8a/libOSMesa_2300d.so
git rm -q -f -- app/src/main/jniLibs/arm64-v8a/libOSMesa_8.so
git rm -q -f -- app/src/main/jniLibs/arm64-v8a/libVkLayer_khronos_timeline_semaphore.so
git rm -q -f -- app/src/main/jniLibs/arm64-v8a/libfishnet.so
git rm -q -f -- app/src/main/jniLibs/arm64-v8a/libglapi.so
git rm -q -f -- app/src/main/jniLibs/arm64-v8a/libglxshim.so
git rm -q -f -- app/src/main/jniLibs/arm64-v8a/libmmkv.so
git rm -q -f -- app/src/main/jniLibs/arm64-v8a/libmobileglues.so
git rm -q -f -- app/src/main/jniLibs/arm64-v8a/libmobileglues_info_getter.so
git rm -q -f -- app/src/main/jniLibs/arm64-v8a/libshaderconv.so
git rm -q -f -- app/src/main/jniLibs/arm64-v8a/libspirv-cross-c-shared.so
git rm -q -f -- app/src/main/jniLibs/arm64-v8a/libsqlcipher.so
git rm -q -f -- app/src/main/jniLibs/arm64-v8a/libterracotta.so
git rm -q -f -- app/src/main/jniLibs/arm64-v8a/libvirgl_test_server.so
git rm -q -f -- app/src/main/jniLibs/arm64-v8a/libvulkan_freedreno.so
git rm -q -f -- app/src/main/jniLibs/arm64-v8a/libzink_dri.so
git rm -q -f -- app/src/main/res/drawable/mc_hero_bg.jpg
git rm -q -f -- app/src/main/res/drawable/nux_badge_android.png
git rm -q -f -- app/src/main/res/drawable/nux_badge_premium.png
git rm -q -f -- app/src/main/res/drawable/nux_badge_verified.png
