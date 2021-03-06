
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux'
import { connectRequest, mutateAsync } from 'redux-query';
import { push } from 'redux-little-router'

import CodelistApi from '../../apis/CodelistApi'

@connect(
    (state, props) => {
        return {
            codelists: state.entities.codelists,
            codelistIds: state.entities.codelistIds,
            codelist: state.entities.codelists && props.urlParams ? state.entities.codelists[props.urlParams._] : null
        }
    },
    (dispatch) => {
        return {
            showCodelist: (id) => {
                dispatch(push(`/codelists/${id}`))
            },
            deleteCodelist: (id) => {
                dispatch(mutateAsync(CodelistApi.deleteCodelistQuery(id)));
            }
        }
    }
)

@connectRequest(
    (props) => {
        if (!props.codelistIds) {
            return CodelistApi.getCodelistsQuery()
        }
        return props.codelistIds.map(id => CodelistApi.getCodelistQuery(id))
    })

export default class Codelists extends Component {

    render() {
        const {codelists, codelistIds, codelist, showCodelist, deleteCodelist, children, ...rest} = this.props;

        const componentProps = {
            codelists,
            codelistIds,
            codelist,
            showCodelist,
            deleteCodelist
        }

        const childrenWithProps = React.Children.map(children,
            (child) => React.cloneElement(child, {}, React.cloneElement(React.Children.only(child.props.children), componentProps))
        );

        return <div>
                   { childrenWithProps }
               </div>
    }
}

Codelists.propTypes = {
    //children: PropTypes.element.isRequired
};

Codelists.defaultProps = {
};