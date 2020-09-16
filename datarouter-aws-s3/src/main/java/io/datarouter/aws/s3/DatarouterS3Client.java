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
package io.datarouter.aws.s3;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import io.datarouter.aws.s3.S3Headers.ContentType;
import io.datarouter.aws.s3.S3Headers.S3ContentType;
import io.datarouter.scanner.Scanner;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.S3Object;

public interface DatarouterS3Client{

	Scanner<Bucket> scanBuckets();

	Region getBucketRegion(String bucket);

	void copyObject(String bucket, String sourceKey, String destinationKey, ObjectCannedACL acl);

	void deleteObject(String bucket, String key);

	void putObjectWithHeartbeat(String bucket, String key, ContentType contentType, Path path, Runnable heartbeat);

	BufferedWriter putAsWriter(String bucket, String key, ContentType contentType);

	OutputStream put(String bucket, String key, S3ContentType contentType);

	void putObjectAsString(String bucket, String key, ContentType contentType, String content);

	void putObjectAsBytes(String bucket, String key, ContentType contentType, String cacheControl, ObjectCannedACL acl,
			byte[] bytes);

	void putPublicObject(String bucket, String key, ContentType contentType, Path path);

	void putObject(String bucket, String key, ContentType contentType, Path path);

	void downloadFilesToDirectory(String bucket, String prefix, Path path);

	Path downloadFileToDirectory(String bucket, String key, Path path);

	void downloadFile(String bucket, String key, Path path);

	void downloadFileWithHeartbeat(String bucket, String key, Path path, Runnable heartbeat);

	Scanner<List<String>> scanBatchesOfLinesWithPrefix(String bucket, String prefix, int batchSize);

	Scanner<String> scanLines(String bucket, String key);

	Scanner<List<String>> scanBatchesOfLines(String bucket, String key, int batchSize);

	InputStream getObject(String bucket, String key);

	byte[] getObjectAsBytes(String bucket, String key);

	String getObjectAsString(String bucket, String key);

	Optional<Instant> findLastModified(String bucket, String key);

	Optional<S3Object> findLastModifiedObjectWithPrefix(String bucket, String prefix);

	URL generateLink(String bucket, String key, Duration expireAfter);

	Scanner<S3Object> listObjects(String bucket, String prefix);

	Scanner<S3Object> scanObjects(String bucket, String prefix, String startAfter, String delimiter);

	Scanner<String> scanPrefixes(String bucket, String prefix, String startAfter, String delimiter);

	List<String> getCommonPrefixes(String bucket, String prefix, String delimiter);

	boolean exists(String bucket, String key);

	boolean existsPrefix(String bucket, String prefix);

}
