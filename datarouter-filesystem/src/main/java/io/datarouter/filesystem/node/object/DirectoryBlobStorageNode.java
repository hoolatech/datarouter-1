/**
 * Copyright © 2009 HotPads (admin@hotpads.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datarouter.filesystem.node.object;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import io.datarouter.model.databean.Databean;
import io.datarouter.model.key.primary.PrimaryKey;
import io.datarouter.model.serialize.fielder.DatabeanFielder;
import io.datarouter.scanner.Scanner;
import io.datarouter.storage.client.ClientType;
import io.datarouter.storage.file.Pathbean;
import io.datarouter.storage.file.PathbeanKey;
import io.datarouter.storage.node.NodeParams;
import io.datarouter.storage.node.op.raw.BlobStorage.PhysicalBlobStorageNode;
import io.datarouter.storage.node.type.physical.base.BasePhysicalNode;
import io.datarouter.storage.util.Subpath;

public class DirectoryBlobStorageNode<
		PK extends PrimaryKey<PK>,
		D extends Databean<PK,D>,
		F extends DatabeanFielder<PK,D>>
extends BasePhysicalNode<PK,D,F>
implements PhysicalBlobStorageNode<PK,D,F>{

	private final DirectoryBlobStorage directoryBlobStorage;
	//keep these for creating subdirectories
	private final String bucket;
	private final Subpath rootPath;

	public DirectoryBlobStorageNode(
			NodeParams<PK,D,F> params,
			ClientType<?,?> clientType,
			DirectoryBlobStorage directoryBlobStorage,
			String bucket,
			Subpath rootPath){
		super(params, clientType);
		this.directoryBlobStorage = directoryBlobStorage;
		this.bucket = bucket;
		this.rootPath = rootPath;
	}

	@Override
	public String getBucket(){
		return bucket;
	}

	@Override
	public Subpath getRootPath(){
		return rootPath;
	}

	@Override
	public boolean exists(PathbeanKey key){
		return directoryBlobStorage.exists(key);
	}

	@Override
	public Optional<Long> length(PathbeanKey key){
		return directoryBlobStorage.length(key);
	}

	@Override
	public byte[] read(PathbeanKey key){
		return directoryBlobStorage.read(key);
	}

	@Override
	public byte[] read(PathbeanKey key, long offset, int length){
		return directoryBlobStorage.read(key, offset, length);
	}

	@Override
	public void write(PathbeanKey key, byte[] content){
		write(key, List.of(content).iterator());
	}

	@Override
	public void write(PathbeanKey key, Iterator<byte[]> chunks){
		directoryBlobStorage.write(key, chunks);
	}

	@Override
	public void write(PathbeanKey key, InputStream inputStream){
		directoryBlobStorage.write(key, inputStream);
	}

	@Override
	public Scanner<List<Pathbean>> scanPaged(Subpath subpath){
		return directoryBlobStorage.scanPaged(subpath);
	}

	@Override
	public Scanner<List<PathbeanKey>> scanKeysPaged(Subpath subpath){
		return directoryBlobStorage.scanKeysPaged(subpath);
	}

	@Override
	public void delete(PathbeanKey key){
		directoryBlobStorage.delete(key);
	}

	@Override
	public void deleteAll(Subpath subpath){
		directoryBlobStorage.deleteAll(subpath);
	}

}
