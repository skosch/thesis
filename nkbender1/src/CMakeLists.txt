
add_executable(thesis_nkbender1 main.cpp Job.cpp)
add_definitions(-DBENDER_MIP)
SET(EXECUTABLE_OUTPUT_PATH "${CMAKE_SOURCE_DIR}/bin")


target_link_libraries (thesis_nkbender1 ilocplex cp concert cplex)

target_link_libraries (thesis_nkbender1 m pthread rt)

target_link_libraries(thesis_nkbender1 gsl gslcblas)
