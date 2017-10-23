/*
 * GetDisplacement.c
 *
 * Code generation for function 'GetDisplacement'
 *
 */

/* Include files */
#include "rt_nonfinite.h"
#include "GetDisplacement.h"
#include "GetDisplacement_emxutil.h"
#include "cumtrapz.h"
#include "GetDisplacement_data.h"

/* Variable Definitions */
static emlrtRTEInfo emlrtRTEI = { 1, 25, "GetDisplacement",
  "D:\\study\\Graduation Project\\matlab visual\\GetDisplacement.m" };

/* Function Definitions */
void GetDisplacement(const emlrtStack *sp, const emxArray_real_T *acceleration,
                     emxArray_real_T *Displacement)
{
  emxArray_real_T *r0;
  emlrtHeapReferenceStackEnterFcnR2012b(sp);
  emxInit_real_T(sp, &r0, 2, &emlrtRTEI, true);
  covrtLogFcn(&emlrtCoverageInstance, 0U, 0);
  covrtLogBasicBlock(&emlrtCoverageInstance, 0U, 0);
  cumtrapz(sp, acceleration, r0);
  cumtrapz(sp, r0, Displacement);
  emxFree_real_T(&r0);
  emlrtHeapReferenceStackLeaveFcnR2012b(sp);
}

/* End of code generation (GetDisplacement.c) */
