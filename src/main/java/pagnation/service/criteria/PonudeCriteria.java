package pagnation.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.BooleanFilter;
import tech.jhipster.service.filter.DoubleFilter;
import tech.jhipster.service.filter.Filter;
import tech.jhipster.service.filter.FloatFilter;
import tech.jhipster.service.filter.IntegerFilter;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.service.filter.StringFilter;

/**
 * Criteria class for the {@link pagnation.domain.Ponude} entity. This class is used
 * in {@link pagnation.web.rest.PonudeResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /ponudes?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
public class PonudeCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private IntegerFilter broj;

    private StringFilter ime;

    private Boolean distinct;

    public PonudeCriteria() {}

    public PonudeCriteria(PonudeCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.broj = other.broj == null ? null : other.broj.copy();
        this.ime = other.ime == null ? null : other.ime.copy();
        this.distinct = other.distinct;
    }

    @Override
    public PonudeCriteria copy() {
        return new PonudeCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public LongFilter id() {
        if (id == null) {
            id = new LongFilter();
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public IntegerFilter getBroj() {
        return broj;
    }

    public IntegerFilter broj() {
        if (broj == null) {
            broj = new IntegerFilter();
        }
        return broj;
    }

    public void setBroj(IntegerFilter broj) {
        this.broj = broj;
    }

    public StringFilter getIme() {
        return ime;
    }

    public StringFilter ime() {
        if (ime == null) {
            ime = new StringFilter();
        }
        return ime;
    }

    public void setIme(StringFilter ime) {
        this.ime = ime;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PonudeCriteria that = (PonudeCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(broj, that.broj) &&
            Objects.equals(ime, that.ime) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, broj, ime, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PonudeCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (broj != null ? "broj=" + broj + ", " : "") +
            (ime != null ? "ime=" + ime + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
