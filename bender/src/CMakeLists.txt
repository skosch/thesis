
set (rhea_VERISON_MAJOR 0)
set (rhea_VERSION_MINOR 1)

add_executable(thesis_bender main.cpp BBNode.cpp RootNode.cpp Job.cpp)
add_definitions(-DBENDER_MIP)
SET(EXECUTABLE_OUTPUT_PATH "${CMAKE_SOURCE_DIR}/bin")

find_package (Boost REQUIRED)
if (BOOST_FOUND)
  include_directories(${Boost_INCLUDE_DIRS})
  target_link_libraries (rhea ${Boost_LIBRARIES})
endif (BOOST_FOUND)

target_link_libraries (thesis_bender ilocplex cp concert cplex)

target_link_libraries (thesis_bender m pthread rt)

target_link_libraries(thesis_bender gsl gslcblas)
