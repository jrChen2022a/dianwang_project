# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
#
cmake_minimum_required(VERSION 3.4.1)
message("CMAKE_SOURCE_DIR:" ${CMAKE_SOURCE_DIR})
##include头文件目录
#include_directories(include/ncnn)
#
set(ncnn_DIR ${CMAKE_SOURCE_DIR}/ncnn-20201218-android/arm64-v8a/lib/cmake/ncnn)
find_package(ncnn REQUIRED)
##添加ncnn库
add_library(libncnn STATIC IMPORTED )
set_target_properties(libncnn
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/ncnn-20201218-android/arm64-v8a/lib/libncnn.a)

find_library(  log-lib log )

#source directory源文件目录
file(GLOB FACE_SRC detect.h
                   recognize.h
                   detect.cpp
                   face_jni.cpp
                   native-lib.cpp
                   recognize.cpp)

set(FACE_COMPILE_CODE ${FACE_SRC})
message("FACE_SRC:" ${FACE_SRC})
message("CMAKE_SOURCE_DIR:" ${CMAKE_SOURCE_DIR})
##编译为动态库
add_library(Face SHARED ${FACE_COMPILE_CODE})
#

##添加工程所依赖的库
target_link_libraries( Face
        libncnn
        android
        jnigraphics
        ${log-lib}
        )


add_library(ynn SHARED yolov5ncnn_jni.cpp)
target_link_libraries(ynn
        libncnn
        android
        jnigraphics
        ${log-lib}
        )

