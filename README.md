# Image Upload Application

Upload photos to server, manage and share cloud photos, preview images full screen, and file information. 

### 1. Upload Images
  - Selected images will be uploaded to server through [FileBackgroundService.kt](app%2Fsrc%2Fmain%2Fjava%2Fcom%2Fashik%2Fimageupload%2Fservice%2FFileBackgroundService.kt). 
  - Maximum 10 images allowed to upload at a time. 
  - Images will be saved to cloud directory in the local storage.

### 2. Delete Images
User can delete one or more selected images. By clicking long press of grid image in home screen selection mode will presence. You can either delete multiple images through [FileBackgroundService.kt](app%2Fsrc%2Fmain%2Fjava%2Fcom%2Fashik%2Fimageupload%2Fservice%2FFileBackgroundService.kt) 

<img src="https://github.com/ashik169/ImageUpload/blob/main/screenshots/Screenshot_20240121_203433_multi_select.png" alt="drawing" width="200"/>

### 3. Receive Images from other apps
If you share any images from gallery app. App icon will be present receiving a images. 

### 4. Share Images to other apps
Can share saved images to other apps one or more images. 

### 5. Permissions
Requires the following permissions picking gallery and take picture:
  - Camera - Take a picture
  - Read External Storage - Android 32 and below
  - Read Media Image - Android 33 and higher
    
#### Home Screen
<img src="https://github.com/ashik169/ImageUpload/blob/main/screenshots/Screenshot_20240121_183711_home.png" alt="drawing" width="250"/>

#### Choose an image Screen
<img src="https://github.com/ashik169/ImageUpload/blob/main/screenshots/Screenshot_20240121_183738_options.png" alt="drawing" width="250"/>

#### Preview/Upload Screen
<img src="https://github.com/ashik169/ImageUpload/blob/main/screenshots/Screenshot_20240121_183811_preview.png" alt="drawing" width="250"/>

#### Gallery Screen
<img src="https://github.com/ashik169/ImageUpload/blob/main/screenshots/Screenshot_20240121_183844_gallery.png" alt="drawing" width="250"/>

#### FileInfo Screen
<img src="https://github.com/ashik169/ImageUpload/blob/main/screenshots/Screenshot_20240121_204713_file_info.png" alt="drawing" width="250"/>


