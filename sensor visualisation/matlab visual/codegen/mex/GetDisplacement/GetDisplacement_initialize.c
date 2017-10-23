/*
 * GetDisplacement_initialize.c
 *
 * Code generation for function 'GetDisplacement_initialize'
 *
 */

/* Include files */
#include "rt_nonfinite.h"
#include "GetDisplacement.h"
#include "GetDisplacement_initialize.h"
#include "_coder_GetDisplacement_mex.h"
#include "GetDisplacement_data.h"

/* Function Declarations */
static void GetDisplacement_once(void);

/* Function Definitions */
static void GetDisplacement_once(void)
{
  /* Allocate instance data */
  covrtAllocateInstanceData(&emlrtCoverageInstance);

  /* Initialize Coverage Information */
  covrtScriptInit(&emlrtCoverageInstance,
                  "D:\\study\\Graduation Project\\matlab visual\\GetDisplacement.m",
                  0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0);

  /* Initialize Function Information */
  covrtFcnInit(&emlrtCoverageInstance, 0, 0, "GetDisplacement", 0, -1, 129);

  /* Initialize Basic Block Information */
  covrtBasicBlockInit(&emlrtCoverageInstance, 0, 0, 56, -1, 124);

  /* Initialize If Information */
  /* Initialize MCDC Information */
  /* Initialize For Information */
  /* Initialize While Information */
  /* Initialize Switch Information */
  /* Start callback for coverage engine */
  covrtScriptStart(&emlrtCoverageInstance, 0U);
}

void GetDisplacement_initialize(void)
{
  emlrtStack st = { NULL, NULL, NULL };

  mexFunctionCreateRootTLS();
  emlrtBreakCheckR2012bFlagVar = emlrtGetBreakCheckFlagAddressR2012b();
  st.tls = emlrtRootTLSGlobal;
  emlrtClearAllocCountR2012b(&st, false, 0U, 0);
  emlrtEnterRtStackR2012b(&st);
  if (emlrtFirstTimeR2012b(emlrtRootTLSGlobal)) {
    GetDisplacement_once();
  }
}

/* End of code generation (GetDisplacement_initialize.c) */
