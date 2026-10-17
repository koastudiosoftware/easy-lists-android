<!--
DRAFT. Verify before publishing:
- The Settings switches for photos, the camera, and tags exist (enablePhotos, enableCamera, enableTags).
  Replace the generic wording below with the real labels from strings.xml.
- Confirm the exact steps for adding a photo (camera vs gallery) against the current screens.
- The 1600 px and WEBP figures come from ImageRepositoryImpl. Update if they change.
- Nothing deletes a photo's file when it is replaced or removed. Don't promise that storage is freed.
-->

# Photos

You can attach a photo to a list item, for example a picture of the exact product you want or the shelf it lives on.

## Adding a photo

Open an item and add a photo by taking one with the camera or choosing one from your device. If you don't want these options, you can turn the camera or photos off in Settings.

After adding a photo, you can zoom and move it inside its frame. Double-tap to zoom in or out, or pinch. The framing you choose is saved with the item.

## What happens to your photo

- **The app saves a smaller copy.** Large photos are scaled down so their longest side is at most 1600 pixels, and saved in a compact format (WEBP). This keeps the app and your backups small. Your original photo isn't changed.
- **Photos stay inside the app.** They don't appear in your phone's gallery, and other apps can't see them.
- **Uninstalling the app removes them.** To keep your photos through an uninstall, include **Photos** in a backup. See [Local backups](local-backups.md).

## Photos in backups

If you tick **Photos** when you make a backup, the photos for your items are saved in it, and they come back when you restore. If you leave **Photos** unticked, your items are backed up without them. When you restore such a backup onto the same phone, items keep any photos the app still has.

