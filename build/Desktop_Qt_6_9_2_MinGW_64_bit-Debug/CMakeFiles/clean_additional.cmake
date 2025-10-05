# Additional clean files
cmake_minimum_required(VERSION 3.16)

if("${CONFIG}" STREQUAL "" OR "${CONFIG}" STREQUAL "Debug")
  file(REMOVE_RECURSE
  "CMakeFiles\\katchau_autogen.dir\\AutogenUsed.txt"
  "CMakeFiles\\katchau_autogen.dir\\ParseCache.txt"
  "katchau_autogen"
  )
endif()
