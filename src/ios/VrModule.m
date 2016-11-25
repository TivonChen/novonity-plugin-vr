//
//  VrModule.m
//
//  Created by ChenTivon on 14/7/16.
//

#import "VrModule.h"

@implementation VrModule

@synthesize videoView;
@synthesize isPaused;
@synthesize currentCallbackId;

- (void)pluginInitialize {
}

- (void) stopPlaying: (CDVInvokedUrlCommand*)command {
    [self stopVideo];
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

}

- (void) startPlaying: (CDVInvokedUrlCommand*)command {
    NSString* playUrl = [command.arguments objectAtIndex: 0];
    
    if (playUrl == nil) {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"arg was null"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
    self.currentCallbackId = command.callbackId;
    
    NSLog(@"VrModule startPlaying = %@", playUrl);
    
    self.videoView  = [[GVRVideoView alloc] initWithFrame:CGRectMake(0, 0, 0, 0)];
    self.videoView.delegate = self;
    self.videoView.enableFullscreenButton = YES;
    self.videoView.enableCardboardButton = YES;
    self.videoView.displayMode = kGVRWidgetDisplayModeFullscreen;
    [self.viewController.view addSubview:self.videoView];
    
    self.isPaused = NO;
    
    if ([playUrl hasPrefix:@"http"]) {
        [self.videoView loadFromUrl:[[NSURL alloc] initWithString:playUrl]];
    } else {
        [self.videoView loadFromUrl:[[NSURL alloc] initFileURLWithPath:playUrl]];
    }
}

#pragma mark - UIButton
- (void) buttonClick {
    [self stopVideo];
}

#pragma mark - GVRVideoViewDelegate

- (void)widgetViewDidTap:(GVRWidgetView *)widgetView {
    if (self.isPaused) {
        [self.videoView resume];
    } else {
        [self.videoView pause];
    }
    self.isPaused = !self.isPaused;
}

- (void)widgetView:(GVRWidgetView *)widgetView didLoadContent:(id)content {
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.currentCallbackId];
}


- (void)widgetView:(GVRWidgetView *)widgetView
didChangeDisplayMode:(GVRWidgetDisplayMode)displayMode{
    if (displayMode != kGVRWidgetDisplayModeFullscreen && displayMode != kGVRWidgetDisplayModeFullscreenVR){
        // Full screen closed, closing the view
        [self stopVideo];
    }
}

- (void)widgetView:(GVRWidgetView *)widgetView
didFailToLoadContent:(id)content
  withErrorMessage:(NSString *)errorMessage {
    NSLog(@"Failed to load video: %@", errorMessage);
    [self stopVideo];
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Failed to load video"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.currentCallbackId];
}

- (void)videoView:(GVRVideoView*)videoView didUpdatePosition:(NSTimeInterval)position {
    // Loop the video when it reaches the end.
    if (position == videoView.duration) {
        NSLog(@"videoView didUpdatePosition: %f", position);
        [self stopVideo];
        // [self.videoView seekTo:0];
        // [self.videoView resume];
    }
}

- (void)stopVideo {
    if (self.videoView != nil) {
        [self.videoView stop];
        [self.videoView removeFromSuperview];
        self.videoView = nil;
    }
}

@end
