//
//  BNKey.m
//  BitnashKit
//
//  Created by Rich Collins on 5/1/14.
//  Copyright (c) 2014 Bitmarkets. All rights reserved.
//

#import "BNKey.h"

@implementation BNKey

- (id)init
{
    self = [super init];
    [self.serializedSlotNames addObjectsFromArray:[NSArray arrayWithObjects:
                                                   @"pubKey",
                                                   @"address",
                                                   @"creationTime",
                                                   nil]];
    return self;
}

- (NSDate *)creationDate
{
    return [NSDate dateWithTimeIntervalSince1970:[_creationTime longValue]/1000];
}

- (NSString *)nodeTitle
{
    return self.address;
}

- (NSString *)nodeSubtitle
{
    NSString *dateString =  [self.creationDate
                             descriptionWithCalendarFormat:@"%Y-%m-%d %H:%M:%S" timeZone:nil
                             //descriptionWithCalendarFormat:@"%x %X %Z" timeZone:nil
            //descriptionWithCalendarFormat:@"%c" timeZone:nil
            locale:[[NSUserDefaults standardUserDefaults] dictionaryRepresentation]];
    return [@"Created " stringByAppendingString:dateString];
}

- (NSUInteger)hash
{
    return [self.address hash];
}

- (BOOL)isEqualTo:(id)object
{
    return [self.address isEqualTo:[object performSelector:@selector(address)]];
}

@end
