
set (rhea_VERISON_MAJOR 0)
set (rhea_VERSION_MINOR 1)

add_executable(thesis_cp main.cpp MinNonzeroDuedateI.cpp)

SET(EXECUTABLE_OUTPUT_PATH "${CMAKE_SOURCE_DIR}/bin")

find_package (Boost REQUIRED)
if (BOOST_FOUND)
  include_directories(${Boost_INCLUDE_DIRS})
  target_link_libraries (rhea ${Boost_LIBRARIES})
endif (BOOST_FOUND)

target_link_libraries (thesis_cp cp cplex concert)

target_link_libraries (thesis_cp m pthread)

target_link_libraries(thesis_cp gsl gslcblas)
