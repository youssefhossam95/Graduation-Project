//
// File: GetDisplacement_emxAPI.h
//
// MATLAB Coder version            : 3.1
// C/C++ source code generated on  : 21-Oct-2017 11:58:25
//
#ifndef GETDISPLACEMENT_EMXAPI_H
#define GETDISPLACEMENT_EMXAPI_H

// Include Files
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include "rtwtypes.h"
#include "GetDisplacement_types.h"

// Function Declarations
extern emxArray_real_T *emxCreateND_real_T(int numDimensions, int *size);
extern emxArray_real_T *emxCreateWrapperND_real_T(double *data, int
  numDimensions, int *size);
extern emxArray_real_T *emxCreateWrapper_real_T(double *data, int rows, int cols);
extern emxArray_real_T *emxCreate_real_T(int rows, int cols);
extern void emxDestroyArray_real_T(emxArray_real_T *emxArray);
extern void emxInitArray_real_T(emxArray_real_T **pEmxArray, int numDimensions);

#endif

//
// File trailer for GetDisplacement_emxAPI.h
//
// [EOF]
//
