#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(PhotoEditor, NSObject)

RCT_EXTERN_METHOD(open:(NSDictionary *)options
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

@end
