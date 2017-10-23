/*
 * File: _coder_GetDisplacement_api.h
 *
 * MATLAB Coder version            : 3.1
 * C/C++ source code generated on  : 21-Oct-2017 11:58:25
 */

#ifndef _CODER_GETDISPLACEMENT_API_H
#define _CODER_GETDISPLACEMENT_API_H

/* Include Files */
#include "tmwtypes.h"
#include "mex.h"
#include "emlrt.h"
#include <stddef.h>
#include <stdlib.h>
#include "_coder_GetDisplacement_api.h"

/* Type Definitions */
#ifndef struct_emxArray_real_T
#define struct_emxArray_real_T

struct emxArray_real_T
{
  real_T *data;
  int32_T *size;
  int32_T allocatedSize;
  int32_T numDimensions;
  boolean_T canFreeData;
};

#endif                                 /*struct_emxArray_real_T*/

#ifndef typedef_emxArray_real_T
#define typedef_emxArray_real_T

typedef struct emxArray_real_T emxArray_real_T;

#endif                                 /*typedef_emxArray_real_T*/

/* Variable Declarations */
extern emlrtCTX emlrtRootTLSGlobal;
extern emlrtContext emlrtContextGlobal;

/* Function Declarations */
extern void GetDisplacement(emxArray_real_T *acceleration, emxArray_real_T
  *Displacement);
extern void GetDisplacement_api(const mxArray *prhs[1], const mxArray *plhs[1]);
extern void GetDisplacement_atexit(void);
extern void GetDisplacement_initialize(void);
extern void GetDisplacement_terminate(void);
extern void GetDisplacement_xil_terminate(void);

#endif

/*
 * File trailer for _coder_GetDisplacement_api.h
 *
 * [EOF]
 */
