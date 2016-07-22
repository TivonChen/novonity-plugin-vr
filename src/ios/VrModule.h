//
//  VrModule.h
//
//  Created by ChenTivon on 14/07/16.
//

#import <Foundation/Foundation.h>
#import <Cordova/CDV.h>
#import "GVRVideoView.h"

@interface VrModule : CDVPlugin <GVRWidgetViewDelegate>

@property (nonatomic, strong) GVRVideoView *videoView;
@property (nonatomic) BOOL isPaused;
@property (nonatomic, strong) NSString* currentCallbackId;

- (void)startPlaying: (CDVInvokedUrlCommand*)command;

@end
