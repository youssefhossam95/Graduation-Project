/*
 * GetDisplacement_terminate.c
 *
 * Code generation for function 'GetDisplacement_terminate'
 *
 */

/* Include files */
#include "rt_nonfinite.h"
#include "GetDisplacement.h"
#include "GetDisplacement_terminate.h"
#include "_coder_GetDisplacement_mex.h"
#include "GetDisplacement_data.h"

/* Function Definitions */
void GetDisplacement_atexit(void)
{
  emlrtStack st = { NULL, NULL, NULL };

  mexFunctionCreateRootTLS();
  st.tls = emlrtRootTLSGlobal;
  emlrtEnterRtStackR2012b(&st);

  /* Free instance data */
  covrtFreeInstanceData(&emlrtCoverageInstance);
  emlrtLeaveRtStackR2012b(&st);
  emlrtDestroyRootTLS(&emlrtRootTLSGlobal);
}

void GetDisplacement_terminate(void)
{
  emlrtStack st = { NULL, NULL, NULL };

  st.tls = emlrtRootTLSGlobal;
  emlrtLeaveRtStackR2012b(&st);
  emlrtDestroyRootTLS(&emlrtRootTLSGlobal);
}

/* End of code generation (GetDisplacement_terminate.c) */
