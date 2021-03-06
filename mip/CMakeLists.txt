cmake_minimum_required (VERSION 2.6)
project (thesis_mip)

SET (CMAKE_C_COMPILER             "/usr/bin/clang")
SET (CMAKE_C_FLAGS                "-Wall -std=c++0x")
SET (CMAKE_C_FLAGS_DEBUG          "-g")
SET (CMAKE_C_FLAGS_MINSIZEREL     "-Os -DNDEBUG")
SET (CMAKE_C_FLAGS_RELEASE        "-O4 -DNDEBUG")
SET (CMAKE_C_FLAGS_RELWITHDEBINFO "-O2 -g")

SET (CMAKE_CXX_COMPILER             "/usr/bin/clang++")
SET (CMAKE_CXX_FLAGS                "-Wall -g -std=c++0x -DIL_STD")
SET (CMAKE_CXX_FLAGS_DEBUG          "-g")
SET (CMAKE_CXX_FLAGS_MINSIZEREL     "-Os -DNDEBUG")
SET (CMAKE_CXX_FLAGS_RELEASE        "-O4 -DNDEBUG")
SET (CMAKE_CXX_FLAGS_RELWITHDEBINFO "-O2 -g")

SET (CMAKE_AR      "/usr/bin/llvm-ar")
SET (CMAKE_LINKER  "/usr/bin/llvm-ld")
SET (CMAKE_NM      "/usr/bin/llvm-nm")
SET (CMAKE_OBJDUMP "/usr/bin/llvm-objdump")
SET (CMAKE_RANLIB  "/usr/bin/llvm-ranlib")

INCLUDE_DIRECTORIES("/home/sebastian/thesis/cpx_studio_12.5/cplex/include")
INCLUDE_DIRECTORIES("/home/sebastian/thesis/cpx_studio_12.5/cpoptimizer/include")
INCLUDE_DIRECTORIES("/home/sebastian/thesis/cpx_studio_12.5/concert/include")

LINK_DIRECTORIES(   "/home/sebastian/thesis/cpx_studio_12.5/cplex/lib/x86-64_sles10_4.1/static_pic")
LINK_DIRECTORIES(   "/home/sebastian/thesis/cpx_studio_12.5/concert/lib/x86-64_sles10_4.1/static_pic")
LINK_DIRECTORIES(   "/home/sebastian/thesis/cpx_studio_12.5/cpoptimizer/lib/x86-64_sles10_4.1/static_pic")

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake/Modules/")

add_subdirectory(src)

