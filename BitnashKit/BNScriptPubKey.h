//
//  BNScriptPubKey.h
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 voluntary.net. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BNObject.h"

@interface BNScriptPubKey : BNObject

- (BOOL)isMultisig;

@end
