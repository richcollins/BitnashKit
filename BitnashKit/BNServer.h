//
//  BitnashJ.h
//  BitnashKit
//
//  Created by Rich Collins on 3/9/14.
//  Copyright (c) 2014 voluntary.net. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BNError.h"

@interface BNServer : BNObject

@property NSString *walletPath;
//path to folder that will contain the wallet and chainstore.

@property NSString *checkpointsPath;
//path to the checkpoints file

@property NSNumber *torSocksPort;

@property BOOL started;

@property NSTask *task;

@property NSFileHandle *taskStandardError;

@property BOOL usesTestNet;

- (void)start;
//Start the wallet child process (BitcoinJ)

- (BOOL)isRunning;


- (id)sendMessage:(NSString *)messageName withObject:(id)object;

- (id)sendMessage:(NSString *)messageName withObject:(id)object withArg:(id)arg;
//send a message to the child and get back an object

- (NSString *)ping:(NSString *)data;

@end
