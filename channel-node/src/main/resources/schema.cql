CREATE KEYSPACE IF NOT EXISTS outgoing WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
CREATE KEYSPACE IF NOT EXISTS incoming WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
CREATE KEYSPACE IF NOT EXISTS channel WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

// PROD
// CREATE KEYSPACE main WITH REPLICATION = {
// 	 'class': 'NetworkTopologyStrategy',
//	'D1': '1',
//	'D2': '1',
//	'D3': '1'

//----------------- OUTGOING ----------------

CREATE TABLE IF NOT EXISTS outgoing.channel_pool (
    sender              blob,
    receiver            blob,
    auditor             blob,
    min_active          int,
    max_active          int,
    deposit             decimal,
    close_timeout       bigint,
    settle_timeout      bigint,
    audit_timeout       bigint,
    close_blocks_count  bigint,
    shutdown            boolean,
    PRIMARY KEY(sender,receiver) 
)
WITH comment='Outgoing channel pools';

CREATE TABLE IF NOT EXISTS outgoing.channel (
    address             blob,
    status              text,
    transferred         decimal,
    current_nonce       bigint,
    synced_nonce        bigint,
    PRIMARY KEY(address) 
)
WITH comment='Outgoing channels'
;

CREATE TABLE IF NOT EXISTS channel.transfer (
    channel_id          blob,
    transfer_id         blob,
    value               decimal,
    locked              boolean,
    signature           blob,
    PRIMARY KEY(channel_id,transfer_id) 
)
WITH comment='Channel transfers'
;

CREATE TABLE IF NOT EXISTS channel.unlock (
    channel_id          blob,
    transfer_id         blob,
    signature           blob,
    PRIMARY KEY(channel_id,transfer_id) 
)
WITH comment='Channel transfer unlocks'
;

//----------------- INCOMING ----------------

CREATE TABLE IF NOT EXISTS incoming.channel (
    address             blob,
    status              text,
    transferred         decimal,
    current_nonce       bigint,
    synced_nonce        bigint,
    PRIMARY KEY(address) 
)
WITH comment='Incoming channels'
;
