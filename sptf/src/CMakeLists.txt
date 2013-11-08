
add_executable(sptf main.cpp)
add_definitions(-DBENDER_MIP)
SET(EXECUTABLE_OUTPUT_PATH "${CMAKE_SOURCE_DIR}/bin")


target_link_libraries (sptf ilocplex cp concert cplex)

target_link_libraries (sptf m pthread rt)

target_link_libraries(sptf gsl gslcblas)
