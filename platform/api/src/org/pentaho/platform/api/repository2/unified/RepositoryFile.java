package org.pentaho.platform.api.repository2.unified;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Immutable repository file. Use the {@link Builder} to create instances.
 * 
 * @author mlowery
 */
public class RepositoryFile implements Comparable<RepositoryFile>, Serializable {

  // ~ Static fields/initializers ======================================================================================

  private static final long serialVersionUID = -6955142003557786114L;

  public static final String SEPARATOR = "/"; //$NON-NLS-1$

  /**
   * Key used in {@link #titleMap} or {@link #descriptionMap} that indicates what string to use when no locale 
   * information is available.
   */
  public static final String ROOT_LOCALE = "rootLocale"; //$NON-NLS-1$

  // ~ Instance fields =================================================================================================

  private final String name;

  private final Serializable id;

  /**
   * Read-only.
   */
  private final Date createdDate;

  private final String creatorId;

  /**
   * Read-only.
   */
  private final Date lastModifiedDate;

  private final boolean folder;

  /**
   * Read-only.
   */
  private final String path;

  private final boolean hidden;

  private final boolean versioned;

  private final long fileSize;

  /**
   * The version name or number. Read-only.
   */
  private final Serializable versionId;

  /**
   * Locked status. Read-only.
   */
  private final boolean locked;

  /**
   * Username of the owner of the lock. Read-only. {@code null} if file not locked.
   */
  private final String lockOwner;

  /**
   * Message left by the owner when he locked the file. Read-only. {@code null} if file not locked.
   */
  private final String lockMessage;

  /**
   * The date that this lock was created. Read-only. {@code null} if file not locked.
   */
  private final Date lockDate;

  /**
   * The owner of this file. Usually plays a role in access control. Read-only.
   */
  private final RepositoryFileSid owner;

  /**
   * A title for the file for the current locale. If locale not available, the file's name is returned. Read-only.
   */
  private final String title;

  /**
   * A description of the file for the current locale. Read-only.
   */
  private final String description;

  /**
   * A map for titles. Keys are locale strings and values are titles. Write-only. {@code null} value means that no 
   * title will be created or updated.
   */
  private final Map<String, String> titleMap;

  /**
   * A map for descriptions. Keys are locale strings and values are descriptions. Write-only. {@code null} value means 
   * that no description will be created or updated.
   */
  private final Map<String, String> descriptionMap;

  /**
   * The locale string with which locale-sensitive fields (like title) are populated. Used in {@link #equals(Object)} 
   * calculation to guarantee caching works correctly. Read-only.
   */
  private final String locale;

  /**
   * The original folder path where the file resided before it was deleted. If this file has been deleted (but not 
   * permanently deleted), then this field will be non-null. Read-only.
   */
  private final String originalParentFolderPath;

  /**
   * The date this file was deleted. If this file has been deleted (but not permanently deleted), then this field will 
   * be non-null. Read-only.
   */
  private final Date deletedDate;

  // ~ Constructors ===================================================================================================

  /*
   * This assumes all Serializables are immutable (because they are not defensively copied).
   */
  public RepositoryFile(Serializable id, String name, boolean folder, boolean hidden, boolean versioned,
      Serializable versionId, String path, Date createdDate, Date lastModifiedDate, boolean locked, String lockOwner,
      String lockMessage, Date lockDate, RepositoryFileSid owner, String locale, String title,
      Map<String, String> titleMap, String description, Map<String, String> descriptionMap,
      String originalParentFolderPath, Date deletedDate, long fileSize, String creatorId) {
    super();
    this.id = id;
    this.name = name;
    this.folder = folder;
    this.hidden = hidden;
    this.versioned = versioned;
    this.versionId = versionId;
    this.path = path;
    this.createdDate = createdDate != null ? new Date(createdDate.getTime()) : null;
    this.lastModifiedDate = lastModifiedDate != null ? new Date(lastModifiedDate.getTime()) : null;
    this.locked = locked;
    this.lockOwner = lockOwner;
    this.lockMessage = lockMessage;
    this.lockDate = lockDate != null ? new Date(lockDate.getTime()) : null;
    this.owner = owner;
    this.locale = locale;
    this.title = title;
    this.titleMap = titleMap != null ? new HashMap<String, String>(titleMap) : null;
    this.description = description;
    this.descriptionMap = descriptionMap != null ? new HashMap<String, String>(descriptionMap) : null;
    this.originalParentFolderPath = originalParentFolderPath;
    this.deletedDate = deletedDate != null ? new Date(deletedDate.getTime()) : null;
    this.fileSize = fileSize;
    this.creatorId = creatorId;
  }
  
  // ~ Methods =========================================================================================================

  public String getName() {
    return name;
  }

  public Serializable getId() {
    return id;
  }

  public Date getCreatedDate() {
    // defensive copy
    return (createdDate != null ? new Date(createdDate.getTime()) : null);
  }

  public String getCreatorId() {
    return creatorId;
  }

  public Date getLastModifiedDate() {
    // defensive copy
    return (lastModifiedDate != null ? new Date(lastModifiedDate.getTime()) : null);
  }

  public Long getFileSize() {
    return fileSize;
  }


  public boolean isFolder() {
    return folder;
  }

  public String getPath() {
    return path;
  }

  public boolean isHidden() {
    return hidden;
  }

  public boolean isVersioned() {
    return versioned;
  }

  public Serializable getVersionId() {
    return versionId;
  }

  public boolean isLocked() {
    return locked;
  }

  public String getLockOwner() {
    return lockOwner;
  }

  public String getLockMessage() {
    return lockMessage;
  }

  public Date getLockDate() {
    // defensive copy
    return (lockDate != null ? new Date(lockDate.getTime()) : null);
  }

  public RepositoryFileSid getOwner() {
    return owner;
  }

  /**
   * Returns title for current locale or file name if not available.
   */
  public String getTitle() {
    return title != null ? title : name;
  }

  public String getDescription() {
    return description;
  }

  public Map<String, String> getTitleMap() {
    // defensive copy
    return titleMap == null ? null : new HashMap<String, String>(titleMap);
  }

  public Map<String, String> getDescriptionMap() {
    // defensive copy
    return descriptionMap == null ? null : new HashMap<String, String>(descriptionMap);
  }

  public String getLocale() {
    return locale;
  }

  public String getOriginalParentFolderPath() {
    return originalParentFolderPath;
  }

  public Date getDeletedDate() {
    return deletedDate != null ? new Date(deletedDate.getTime()) : null;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public static class Builder {

    private String name;

    private Serializable id;

    private Date createdDate;

    private String creatorId;

    private Date lastModifiedDate;

    private long fileSize;

    private boolean folder;

    private String path;

    private boolean hidden;

    private boolean versioned;

    private Serializable versionId;

    private boolean locked;

    private String lockOwner;

    private String lockMessage;

    private Date lockDate;

    private RepositoryFileSid owner;

    private String title;

    private String description;

    private Map<String, String> titleMap;

    private Map<String, String> descriptionMap;

    private String locale;

    private String originalParentFolderPath;

    private Date deletedDate;

    public Builder(final String name) {
      this.name = name;
      this.clearTitleMap();
    }

    public Builder(final Serializable id, final String name) {
      notNull(id);
      this.name = name;
      this.id = id;
      this.clearTitleMap();
    }

    public Builder(final RepositoryFile other) {
      this(other.name);
      this.id(other.id).path(other.path).createdDate(other.createdDate).creatorId(other.creatorId).fileSize(other.fileSize).folder(other.folder).lastModificationDate(
          other.lastModifiedDate).versioned(other.versioned).hidden(other.hidden).versionId(other.versionId).locked(
          other.locked).lockDate(other.lockDate).lockOwner(other.lockOwner).lockMessage(other.lockMessage).owner(
          other.owner).title(other.title).description(other.description).titleMap(other.titleMap).descriptionMap(
          other.descriptionMap).locale(other.locale).originalParentFolderPath(other.originalParentFolderPath)
          .deletedDate(other.deletedDate);
    }

    public RepositoryFile build() {
      return new RepositoryFile(id, name, this.folder, this.hidden, this.versioned, this.versionId, this.path,
          this.createdDate, this.lastModifiedDate, this.locked, this.lockOwner, this.lockMessage, this.lockDate,
          this.owner, this.locale, this.title, this.titleMap, this.description, this.descriptionMap,
          this.originalParentFolderPath, this.deletedDate, this.fileSize, this.creatorId);
    }

    public Builder createdDate(final Date createdDate1) {
      this.createdDate = createdDate1;
      return this;
    }

    public Builder creatorId(final String creatorId1) {
      this.creatorId = creatorId1;
      return this;
    }

    public Builder lastModificationDate(final Date lastModifiedDate1) {
      // defensive copy
      this.lastModifiedDate = lastModifiedDate1;
      return this;
    }

    /**
     * @param length
     * @return
     */
    public Builder fileSize(long fileSize1) {
      this.fileSize = fileSize1;
      return this;
    }

    public Builder folder(final boolean folder1) {
      this.folder = folder1;
      return this;
    }
    
    public Builder id(final Serializable id1) {
      this.id = id1;
      return this;
    }
    
    public Builder name(final String name1) {
      this.name = name1;
      return this;
    }

    public Builder path(final String path1) {
      this.path = path1;
      return this;
    }

    public Builder hidden(final boolean hidden1) {
      this.hidden = hidden1;
      return this;
    }

    public Builder versioned(final boolean versioned1) {
      this.versioned = versioned1;
      return this;
    }

    public Builder versionId(final Serializable versionId1) {
      this.versionId = versionId1;
      return this;
    }

    public Builder locked(final boolean locked1) {
      this.locked = locked1;
      return this;
    }

    public Builder lockOwner(final String lockOwner1) {
      this.lockOwner = lockOwner1;
      return this;
    }

    public Builder lockMessage(final String lockMessage1) {
      this.lockMessage = lockMessage1;
      return this;
    }

    public Builder lockDate(final Date lockDate1) {
      // defensive copy
      this.lockDate = lockDate1;
      return this;
    }

    public Builder owner(final RepositoryFileSid owner1) {
      this.owner = owner1;
      return this;
    }

    public Builder originalParentFolderPath(final String originalParentFolderPath1) {
      this.originalParentFolderPath = originalParentFolderPath1;
      return this;
    }

    public Builder deletedDate(final Date deletedDate1) {
      this.deletedDate = deletedDate1;
      return this;
    }

    public Builder title(final String title1) {
      this.title = title1;
      return this;
    }

    public Builder description(final String description1) {
      this.description = description1;
      return this;
    }

    public Builder titleMap(final Map<String, String> titleMap1) {
      this.titleMap = titleMap1;
      return this;
    }

    public Builder clearTitleMap() {
      if (this.titleMap != null) {
        this.titleMap.clear();
      }
      return this;
    }

    public Builder title(final String localeString, final String title1) {
      initTitleMap();
      this.titleMap.put(localeString, title1);
      return this;
    }

    private void initTitleMap() {
      if (this.titleMap == null) {
        this.titleMap = new HashMap<String, String>();
        this.titleMap.put(ROOT_LOCALE, this.name);
      }
    }

    public Builder descriptionMap(final Map<String, String> descriptionMap1) {
      // defensive copy
      this.descriptionMap = descriptionMap1;
      return this;
    }

    public Builder clearDescriptionMap() {
      if (this.descriptionMap != null) {
        this.descriptionMap.clear();
      }
      return this;
    }

    public Builder description(final String localeString, final String description1) {
      initDescriptionMap();
      this.descriptionMap.put(localeString, description1);
      return this;
    }

    private void initDescriptionMap() {
      if (this.descriptionMap == null) {
        this.descriptionMap = new HashMap<String, String>();
      }
    }

    public Builder locale(final String locale1) {
      this.locale = locale1;
      return this;
    }

    private void notNull(final Object in) {
      if (in == null) {
        throw new IllegalArgumentException();
      }
    }

  }

  public int compareTo(final RepositoryFile other) {
    if (other == null) {
      throw new NullPointerException(); // per Comparable contract
    }
    if (equals(other)) {
      return 0;
    }
    // either this or other has a null id; fall back on name
    return getTitle().compareTo(other.getTitle());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((locale == null) ? 0 : locale.hashCode());
    result = prime * result + ((versionId == null) ? 0 : versionId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RepositoryFile other = (RepositoryFile) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (locale == null) {
      if (other.locale != null)
        return false;
    } else if (!locale.equals(other.locale))
      return false;
    if (versionId == null) {
      if (other.versionId != null)
        return false;
    } else if (!versionId.equals(other.versionId))
      return false;
    return true;
  }

}
