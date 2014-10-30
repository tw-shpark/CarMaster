CarMaster
=========

<차세대 차량용 융복합 단말기 기술개발>과제의 Android 어플리케션 GIT

Eclipse Git Setting
-------------------
### Install JGIT Add-on ###
1. Help -> Install New Software
2. add <http://download.eclipse.org/egit/updates> repository
3. 모든 애드온에 체크 (Eclipse Git Team Provider, JGit)
4. Next누르고, 안내에 따라 설치 진행

### Eclipse Git setting ###
1. Window -> Open Perspective -> Other -> Git -> OK
2. 우측 상단에 Git Perspective 추가됨
3. Clone Git Repository
4. URI : https://github.com/tw-shpark/CarMaster.git
5. branch 선택 후, cloning 진행
6. JAVA Perspective로 돌아와서 git디렉토리를 workspace로 Import

Eclipse Git Repository 이용법은 인터넷을 참조.

OpenCV Setting
--------------

### Open CV Library 받는 곳 ###
https://sourceforge.net/projects/opencvlibrary/files/opencv-android/2.4.8/OpenCV-2.4.8-android-sdk.zip/download

#### 설정 ####
1. 압축 해제 후, Eclipse Project로 import.
2. Carmaster project properties -> Android -> Library -> Add -> OpenCV Library - 2.4.8 추가

### NDK Build ###
#### Project properties -> C/C++ Build ####
1. Builder Settings의 Build command -> ${NDKROOT}/ndk-build로 설정
2. Behaviour -> Build on resource save (Auto build)에 체크
3. Behaviour -> Build (Increamental build)에 체크
4. Behaviour -> Clean에 언체크!!

#### Project properties -> Paths and Symbols -> Includes -> GNU C++ ####
1. add -> ${NDKROOT}/platforms/android-9/arch-arm/usr/include
2. add -> ${NDKROOT}/sources/cxx-stl/gnu-libstdc++/4.8/include
3. add -> ${NDKROOT}/sources/cxx-stl/gnu-libstdc++/4.8/libs/armeabi-v7a/include
4. add -> ${ProjDirPath}/../../../OpenCV-2.4.8-android-sdk/sdk/native/jni/include

주의) 

* NDK루트로부터 include디렉토리를 설정 (상대경로)
* 프로젝트 경로로부터 OpenCV include디렉토리를 설정 (상대경로)

Bugfix for Carmaster Project (OpenCV)
-------------------------------------
* OpenCV Library - 2.4.8 Project/src/org.opencv.android/JavaCameraView.java에서 

JavaCameraView.java : Line 143

```
  Size frameSize = calculateCameraFrameSize(sizes, new JavaCameraSizeAccessor(), width, height);
  frameSize.width=640;
  frameSize.height=480;
                    
  params.setPreviewFormat(ImageFormat.NV16); // for comus YCbCr NV21->NV16
  Log.d(TAG, "Set preview size to " + Integer.valueOf((int)frameSize.width) + "x" + Integer.valueOf((int)frameSize.height));
  params.setPreviewSize((int)frameSize.width, (int)frameSize.height);
```

JavaCameraView.java
```java
  int size = (mFrameWidth+160) * (mFrameHeight+45);
  size  = size * ImageFormat.getBitsPerPixel(params.getPreviewFormat()) / 8;
  mBuffer = new byte[size];
```
