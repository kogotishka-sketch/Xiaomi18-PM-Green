# Floyd Video Wallpaper

Android live wallpaper app using the included video.

## Build APK automatically on GitHub (no Android Studio needed)
1. Create a new **private** or public repository on GitHub.
2. Upload the contents of this folder (not the ZIP itself), keeping `.github/workflows/build.yml` included.
3. Open the repository's **Actions** tab and enable Actions if prompted.
4. Choose **Build wallpaper APK** on the left, click **Run workflow**, then the green **Run workflow** button.
5. When the run completes successfully, open it and download the `Floyd-Video-Wallpaper-APK` artifact. Unzip it to get `app-debug.apk`.

The APK is unsigned for Play Store distribution but can be installed manually for testing. Device/Google Wallpapers behavior must be tested on the target phone; this app is a video live wallpaper, not a full OEM 3D Super Wallpaper.
