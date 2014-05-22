//
//  BNWallet.m
//  BitnashKit
//
//  Created by Rich Collins on 3/8/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNWallet.h"
#import "BNTx.h"
#import "NSObject+BNJSON.h"
#import "NSString+BNJSON.h"

@implementation BNWallet

+ (NSArray *)jsonProperties
{
    return [NSArray array];
}

- (id)init
{
    self = [super init];
    self.server = [[BNServer alloc] init];
    self.nodeTitle = @"Wallet";
    self.nodeSuggestedWidth = 200;
    self.shouldSortChildren = NO;
    self.nodeSubtitle = @"starting ...";
    self.transactionsNode = [[BNTransactionsNode alloc] init];
    self.transactionsNode.wallet = self;

    return self;
}

- (void)setNodeSubtitle:(NSString *)nodeSubtitle
{
    if (![[super nodeSubtitle] isEqualToString:nodeSubtitle])
    {
        [super setNodeSubtitle:nodeSubtitle];
        [self postParentChainChanged];
    }
}

- (void)fetch
{
    if (self.isRunning)
    {
        self.refreshInterval = 5.0;
        
        self.nodeSubtitle = [NSString stringWithFormat:@"%.4f BTC", self.balance.floatValue*0.00000001];
        
        if (self.children.count == 0)
        {
            [self setChildren:[NSMutableArray arrayWithObjects:self.depositKey, self.transactionsNode, nil]];
            [self setRefreshInterval:10.0];
            [self postParentChainChanged];
        }
        
        self.nodeNote = nil;
    }
    else
    {
        self.nodeSubtitle = [self status];
        NSNumber *progress = [self progress];
        if (progress)
        {
            self.nodeNote = [NSString stringWithFormat:@"%d%%", (int)roundf(progress.floatValue*100)];
        }
        [self postParentChainChanged];
    }
}

- (void)setPath:(NSString *)path
{
    _server.walletPath = path;
}

- (void)setCheckpointsPath:(NSString *)path
{
    _server.checkpointsPath = path;
}

- (BNError *)error
{
    return _server.error;
}

- (NSNumber *)balance
{
    return [_server sendMessage:@"balance" withObject:self withArg:nil];
}

- (BNKey *)createKey
{
    return [_server sendMessage:@"createKey" withObject:self withArg:nil];
}

- (BNTx *)newTx
{
    BNTx *tx = [[BNTx alloc] init];
    tx.wallet = self;
    return tx;
}

- (NSArray *)transactions
{
    NSArray *transactions = [_server sendMessage:@"transactions" withObject:self];
    for (BNTx *tx in transactions)
    {
        tx.wallet = self;
    }
    return transactions;
}

- (NSArray *)keys
{
    return [_server sendMessage:@"keys" withObject:self];
}

- (BNDepositKey *)depositKey
{
    BNDepositKey *depositKey = [[BNDepositKey alloc] init];
    depositKey.bnParent = self;
    [depositKey copySlotsFrom:[_server sendMessage:@"depositKey" withObject:self]];
    return depositKey;
}

- (NSString *)status
{
    return [_server sendMessage:@"status" withObject:self];
}

- (NSNumber *)progress
{
    return [_server sendMessage:@"progress" withObject:self];
}

- (void)setPassphrase:(NSString *)passphrase
{
    [_server sendMessage:@"setPassphrase" withObject:self withArg:passphrase];
}

- (BOOL)isRunning
{
    return [self.status isEqualToString:@"started"];
}

- (BNTx *)newWithdrawalTxToAddress:(NSString *)address withValue:(NSNumber *)value
{
    BNTx *tx = [self newTx];
    [tx payToAddress:address value:value];
    [tx addInputsAndChange];
    [tx subtractFee];
    return tx;
}


@end
