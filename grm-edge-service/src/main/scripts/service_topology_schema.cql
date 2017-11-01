CREATE KEYSPACE topology
WITH REPLICATION = {'class' : 'SimpleStrategy','replication_factor':1}; 

CREATE TABLE servicedefinition (
  servicedefinitionid text,
  availabilityrules text,
  availabilityruleslockedbyuid text,
  createdby text,
  createdtimestamp timestamp,
  deletetime bigint,
  environment text,
  metadata text,
  partner text,
  policymetadata text,
  policymetadatalockedbyuid text,
  properties list<text>,
  routingmetadatalockedbyuid text,
  servicedefinitionname text,
  servicenamespaceid text,
  updatedby text,
  updatedtimestamp timestamp,
  PRIMARY KEY ((servicedefinitionid))
) WITH
  bloom_filter_fp_chance=0.010000 AND
  caching='ALL' AND
  comment='' AND
  dclocal_read_repair_chance=0.100000 AND
  gc_grace_seconds=864000 AND
  index_interval=128 AND
  read_repair_chance=0.000000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'sstable_size_in_mb': '256', 'tombstone_threshold': '0.05', 'class': 'LeveledCompactionStrategy'} AND
  compression={'sstable_compression': 'SnappyCompressor'};


CREATE TABLE serviceversiondefinition (
  serviceversiondefinitionid text,
  containerversiondefinitionname text,
  createdby text,
  createdtimestamp timestamp,
  deletetime bigint,
  environment text,
  lrm text,
  majorversion int,
  metadata text,
  minorversion int,
  partner text,
  patchversion text,
  properties list<text>,
  reregistrationinterval int,
  servicedefinitionname text,
  servicenamespaceid text,
  serviceversiondefinitionname text,
  updatedby text,
  updatedtimestamp timestamp,
  ver text,
  PRIMARY KEY ((serviceversiondefinitionid))
) WITH
  bloom_filter_fp_chance=0.010000 AND
  caching='ALL' AND
  comment='' AND
  dclocal_read_repair_chance=0.100000 AND
  gc_grace_seconds=864000 AND
  index_interval=128 AND
  read_repair_chance=0.000000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'sstable_size_in_mb': '256', 'tombstone_threshold': '0.05', 'class': 'LeveledCompactionStrategy'} AND
  compression={'sstable_compression': 'SnappyCompressor'};


CREATE TABLE serviceendpoint (
  serviceendpointid text,
  clientsupportedversions text,
  containerinstancename text,
  containerversionname text,
  contextpath text,
  createdby text,
  createdtimestamp timestamp,
  deletetime bigint,
  dme2jdbcdatabasename text,
  dme2jdbchealthcheckdriver text,
  dme2jdbchealthcheckpassword text,
  dme2jdbchealthcheckuser text,
  dme2version text,
  environment text,
  ep text,
  eventcheckmessage text,
  eventcheckstatus text,
  eventcheckstatuscode text,
  eventchecktime timestamp,
  expirationtime timestamp,
  hostaddress text,
  latitude text,
  listenport text,
  longitude text,
  lrm text,
  majorversion int,
  metadata text,
  minorversion int,
  patchversion text,
  properties list<text>,
  protocol text,
  registrationtime timestamp,
  routeoffer text,
  servicedefinitionname text,
  serviceendpointname text,
  servicenamespaceid text,
  serviceversiondefinitionname text,
  status text,
  statuschecktime timestamp,
  statusreasoncode text,
  statusreasondescription text,
  updatedby text,
  updatedtimestamp timestamp,
  PRIMARY KEY ((serviceendpointid))
) WITH
  bloom_filter_fp_chance=0.010000 AND
  caching='ALL' AND
  comment='' AND
  dclocal_read_repair_chance=0.100000 AND
  gc_grace_seconds=864000 AND
  index_interval=128 AND
  read_repair_chance=0.000000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'sstable_size_in_mb': '256', 'tombstone_threshold': '0.05', 'class': 'LeveledCompactionStrategy'} AND
  compression={'sstable_compression': 'SnappyCompressor'};

CREATE INDEX serviceversiondefinition_environment_idx ON serviceversiondefinition (environment);

CREATE INDEX serviceversiondefinition_servicedefinitionname_idx ON serviceversiondefinition (servicedefinitionname);

CREATE INDEX serviceversiondefinition_servicenamespaceId_idx ON serviceversiondefinition (servicenamespaceid);

CREATE INDEX servicedefinition_environment_idx ON servicedefinition (environment);

CREATE INDEX servicedefinition_servicenamespaceId_idx ON servicedefinition (servicenamespaceid);

CREATE INDEX serviceendpoint_containerinstancename_idx ON serviceendpoint (containerinstancename);

CREATE INDEX serviceendpoint_containerVersionName_idx ON serviceendpoint (containerversionname);

CREATE INDEX serviceendpoint_environment_idx ON serviceendpoint (environment);

CREATE INDEX serviceendpoint_hostaddress_idx ON serviceendpoint (hostaddress);

CREATE INDEX serviceendpoint_listenport_idx ON serviceendpoint (listenport);

CREATE INDEX serviceendpoint_routeoffer_idx ON serviceendpoint (routeoffer);

CREATE INDEX serviceendpoint_servicedefinitionname_idx ON serviceendpoint (servicedefinitionname);

CREATE INDEX serviceendpoint_servicenamespaceId_idx ON serviceendpoint (servicenamespaceid);

