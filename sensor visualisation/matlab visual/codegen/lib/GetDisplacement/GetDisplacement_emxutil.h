//
// File: GetDisplacement_emxutil.h
//
// MATLAB Coder version            : 3.1
// C/C++ source code generated on  : 21-Oct-2017 11:58:25
//
#ifndef GETDISPLACEMENT_EMXUTIL_H
#define GETDISPLACEMENT_EMXUTIL_H

// Include Files
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include "rtwtypes.h"
#include "GetDisplacement_types.h"

// Function Declarations
extern void emxEnsureCapacity(emxArray__common *emxArray, int oldNumel, int
  elementSize);
extern void emxFree_real_T(emxArray_real_T **pEmxArray);
extern void emxInit_real_T(emxArray_real_T **pEmxArray, int numDimensions);

#endif

//
// File trailer for GetDisplacement_emxutil.h
//
// [EOF]
//
