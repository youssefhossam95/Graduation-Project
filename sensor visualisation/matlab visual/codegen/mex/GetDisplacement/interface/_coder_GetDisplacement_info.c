/*
 * _coder_GetDisplacement_info.c
 *
 * Code generation for function '_coder_GetDisplacement_info'
 *
 */

/* Include files */
#include "rt_nonfinite.h"
#include "GetDisplacement.h"
#include "_coder_GetDisplacement_info.h"
#include "GetDisplacement_data.h"

/* Function Definitions */
mxArray *emlrtMexFcnProperties(void)
{
  mxArray *xResult;
  mxArray *xEntryPoints;
  const char * fldNames[4] = { "Name", "NumberOfInputs", "NumberOfOutputs",
    "ConstantInputs" };

  mxArray *xInputs;
  const char * b_fldNames[4] = { "Version", "ResolvedFunctions", "EntryPoints",
    "CoverageInfo" };

  xEntryPoints = emlrtCreateStructMatrix(1, 1, 4, fldNames);
  xInputs = emlrtCreateLogicalMatrix(1, 1);
  emlrtSetField(xEntryPoints, 0, "Name", mxCreateString("GetDisplacement"));
  emlrtSetField(xEntryPoints, 0, "NumberOfInputs", mxCreateDoubleScalar(1.0));
  emlrtSetField(xEntryPoints, 0, "NumberOfOutputs", mxCreateDoubleScalar(1.0));
  emlrtSetField(xEntryPoints, 0, "ConstantInputs", xInputs);
  xResult = emlrtCreateStructMatrix(1, 1, 4, b_fldNames);
  emlrtSetField(xResult, 0, "Version", mxCreateString("9.0.0.341360 (R2016a)"));
  emlrtSetField(xResult, 0, "ResolvedFunctions", (mxArray *)
                emlrtMexFcnResolvedFunctionsInfo());
  emlrtSetField(xResult, 0, "EntryPoints", xEntryPoints);
  emlrtSetField(xResult, 0, "CoverageInfo", covrtSerializeInstanceData
                (&emlrtCoverageInstance));
  return xResult;
}

const mxArray *emlrtMexFcnResolvedFunctionsInfo(void)
{
  const mxArray *nameCaptureInfo;
  const char * data[9] = {
    "789ced5a4f4fdb3014772756b10388b1c3b6cb04b749480ddb619b3830d828ff1965431a1242c5242ef570ecca494be154ed3021edb28fb30fb249fb32936637"
    "09042f6a42924211b614a52ff1b3dfbf9ffd9e535058d900a28d8aabfd1a80a2b80f8beb1ef0da7d9f2e886bdcbf7bcf87c0884f7f1597c9a88bdaaef792421b",
    "81a059ccc6145277fba48100470e232d6475dfd43041dbd846eb2c442c6341d88ba157e7847cc5ebcef9c8808409af493d1ae0428fa1083d66437a8cf9f46e79"
    "6f61c670dca675622c716835a18b199da870f60599ae614397c0838916769a90184bc85dc04e834013d988ba25db9bf74dccbcc3cabc92369bb6cb61e334097f",
    "51e12f76addb3c20c89bbf13c3ff41e197f4eecafa4e79effd8c21343de4d09e90c6768c8df9edf5f977c6c797d32f5e41c3658c1cb0b6816c62107ce05bc3b0"
    "a00b6b4d6a043a44d9a11821472124c703ffb9680fdf3efd3d9f81bfdb0681ff7988bf10c10f42f734fdcf406f3f7f0697fd2ce97cfd3c29efc78c1f75e5a9c4",
    "c8f34c9147d226b3102f61b168700a498932fa09d343825c461770423cc5e1e15aec24afa9ae36c654a08ea1a82380913eae3aa31a1703848b2bf83b0d2ec4f0"
    "55316cb5c63861ac51652dc46b841d57cd3a328f3c3da77b8c1bb4f0b8817d7ec4c8b3aff0eda7b14f68ddf0cdd5439f929d01178f342e92f5ff097afbfd1bb8",
    "ec7749f7dbef93bd3b54eb8834108f8ff782ffec42fe21809d1a967ccb317c8f15bd25adec4b985aa8bd425d2f3f8e196f51196f31ad1d6bb88dac0613521852"
    "179d575d0f4e6edabf7331f38f28f34b1a3bb469238e4d579657e9f062d6214f523f6c2af36fe6a27f48fe4c79928ef3bbb21fa4a90bc4d0366cf7171fabcabc",
    "ab59ed46c40fc3935cae1181ff52c697ce9712f6bfabf8c0f4b6e203538d8f0bfe7ee3e30cf4f6f3759f3fe590e73b262490970f733977eac4f05714fe4a1afb"
    "449e43046a64a9abc1d89f4d8d8324fd3b6030fc3c172347d2baa1df71afeb071df7fd5adf927e6f93f5b6d856ead711efb9e53bca7981943f877c47c77bc2fe",
    "71f9ce0eb8ec6749e71def9335cc1dd73bd7d4df15f477058d8bff7191f63b41d67de0a6cf8df5faafebdd2bd6bb0dce2c079fa25b5eef066a64aa779d91f22f"
    "8d8304fd3b6030fcacf31f9dff0c021e066d5fd0b8d0b8d0b8b87abef4449147d251ff03aa90a69347be14679f2d857f2b8d7d22f7d1733582e3a32cf134fbd7",
    "d17993c643feffb3d3f8b86883c0aff171599e71451e492bf16ce156b4fdfb515faf29fc6b69ec138907a1868f840c79d3f7e296ce9be2faff03709bb2d0",
    "" };

  nameCaptureInfo = NULL;
  emlrtNameCaptureMxArrayR2016a(data, 14216U, &nameCaptureInfo);
  return nameCaptureInfo;
}

/* End of code generation (_coder_GetDisplacement_info.c) */
